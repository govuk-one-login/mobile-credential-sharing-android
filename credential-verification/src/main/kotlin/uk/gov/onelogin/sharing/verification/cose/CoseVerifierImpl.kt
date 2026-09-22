package uk.gov.onelogin.sharing.verification.cose

import dev.zacsweers.metro.Inject
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UntrustedCertificate
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CertificateHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateChainValidator
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificateProfileValidator
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificatePurpose
import uk.gov.onelogin.sharing.verification.cose.internal.signature.CoseSignatureVerifier

/**
 * Production implementation of [CoseVerifier] that composes the strict C2-C9
 * verification pipeline.
 */
@Inject
internal class CoseVerifierImpl(
    private val decoder: CoseSign1Decoder,
    private val headerValidator: CertificateHeaderValidator,
    private val pathValidator: CertificateChainValidator,
    private val profileValidator: CertificateProfileValidator,
    private val signatureVerifier: CoseSignatureVerifier
) : CoseVerifier {

    override fun verify(request: CoseVerificationRequest): CoseVerificationResult = when (request) {
        is CoseVerificationRequest.Attached -> verifyAttached(request)
        is CoseVerificationRequest.Detached -> verifyDetached(request)
        is CoseVerificationRequest.KeyBased -> verifyKeyBased(request)
    }

    private fun verifyAttached(
        request: CoseVerificationRequest.Attached
    ): CoseVerificationResult.Attached {
        val trustedRoots = request.trustedRoots
        if (trustedRoots.isEmpty()) throw UntrustedCertificate()

        val coseSign1 = decoder.decode(request.coseSign1Bytes)
        val payload = coseSign1.payload ?: throw MalformedCoseSign1

        val headerProfile = headerValidator.validate(coseSign1)

        val certFactory = CertificateFactory.getInstance("X.509")
        val chain = headerProfile.chain.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        checkChainEmbeddedRoots(chain, trustedRoots)

        val verifiedLeaf =
            verifyChainAgainstRoots(chain, trustedRoots, CertificatePurpose.ISSUER_AUTH)
        val publicKey = extractEcPublicKey(verifiedLeaf)

        signatureVerifier.verify(coseSign1, publicKey, payload)

        return CoseVerificationResult.Attached(
            leafCertificate = verifiedLeaf,
            payload = payload
        )
    }

    private fun verifyDetached(
        request: CoseVerificationRequest.Detached
    ): CoseVerificationResult.Detached {
        val trustedRoots = request.trustedRoots
        if (trustedRoots.isEmpty()) throw UntrustedCertificate()

        val coseSign1 = decoder.decode(request.coseSign1Bytes)
        if (coseSign1.payload != null) throw MalformedCoseSign1

        val headerProfile = headerValidator.validate(coseSign1)

        val certFactory = CertificateFactory.getInstance("X.509")
        val chain = headerProfile.chain.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        checkChainEmbeddedRoots(chain, trustedRoots)

        val verifiedLeaf =
            verifyChainAgainstRoots(chain, trustedRoots, CertificatePurpose.READER_AUTH)
        val publicKey = extractEcPublicKey(verifiedLeaf)

        signatureVerifier.verify(coseSign1, publicKey, request.detachedPayload)

        return CoseVerificationResult.Detached(leafCertificate = verifiedLeaf)
    }

    private fun verifyKeyBased(
        request: CoseVerificationRequest.KeyBased
    ): CoseVerificationResult.KeyBased {
        val coseSign1 = decoder.decode(request.coseSign1Bytes)
        if (coseSign1.payload != null) throw MalformedCoseSign1

        val publicKey = request.publicKey
        val curveSize = publicKey.params.order.bitLength()
        if (curveSize != P256_CURVE_SIZE) {
            throw CoseVerificationFailure.UnsupportedAlgorithm
        }

        signatureVerifier.verify(coseSign1, publicKey, request.detachedPayload)

        return CoseVerificationResult.KeyBased
    }

    private fun checkChainEmbeddedRoots(
        chain: List<X509Certificate>,
        trustedRoots: List<X509Certificate>
    ) {
        for (cert in chain) {
            val certEncoded = cert.encoded
            for (root in trustedRoots) {
                if (certEncoded.contentEquals(root.encoded)) {
                    throw UntrustedCertificate()
                }
            }
        }
    }

    private fun verifyChainAgainstRoots(
        chain: List<X509Certificate>,
        trustedRoots: List<X509Certificate>,
        purpose: CertificatePurpose
    ): X509Certificate {
        var lastUntrustedFailure = UntrustedCertificate()

        for (root in trustedRoots) {
            try {
                pathValidator.verify(chain, root)
                return profileValidator.validate(chain, purpose)
            } catch (e: UntrustedCertificate) {
                lastUntrustedFailure = e
            }
        }

        throw lastUntrustedFailure
    }

    private fun extractEcPublicKey(verifiedLeaf: X509Certificate): ECPublicKey = try {
        verifiedLeaf.publicKey as ECPublicKey
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        throw UntrustedCertificate(cause = e)
    }

    private companion object {
        const val P256_CURVE_SIZE = 256
    }
}
