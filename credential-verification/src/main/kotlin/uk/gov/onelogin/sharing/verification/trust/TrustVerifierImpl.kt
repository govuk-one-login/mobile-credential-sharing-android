package uk.gov.onelogin.sharing.verification.trust

import dev.zacsweers.metro.ContributesBinding
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

@ContributesBinding(CredentialVerificationScope::class)
class TrustVerifierImpl internal constructor(
    private val coseSign1Decoder: CoseSign1Decoder,
    private val signatureVerifier: CoseSignatureVerifier
) : TrustVerifier {

    @OptIn(ExperimentalTime::class)
    override fun verifyCOSESign1(data: ByteArray, trustedRoot: X509Certificate): IssuerAuthResult {
        val coseSign1 = coseSign1Decoder.decode(data)
        val x5chain = coseSign1Decoder.extractX5Chain(coseSign1)

        val certFactory = CertificateFactory.getInstance("X.509")
        val certs = x5chain.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        val ordered = orderCertificates(certs)
        val leaf = ordered.first()

        val payload = coseSign1.payload
            ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)

        signatureVerifier.verify(
            coseSign1,
            leaf.publicKey as ECPublicKey,
            payload,
            VerificationError.INVALID_ISSUER_SIGNATURE
        )

        val validityPeriod = CertificateValidityPeriod(
            notBefore = leaf.notBefore.toInstant().toKotlinInstant(),
            notAfter = leaf.notAfter.toInstant().toKotlinInstant()
        )

        val subjectName = parseSubjectName(leaf)

        return IssuerAuthResult(
            certificateValidityPeriod = validityPeriod,
            msoPayload = payload,
            subjectCountry = subjectName[OID_COUNTRY]
                ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH),
            subjectState = subjectName[OID_STATE_OR_PROVINCE]
        )
    }

    override fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray) {
        val coseSign1 = try {
            coseSign1Decoder.decode(coseData)
        } catch (_: VerificationResult.Failure) {
            throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
        }
        signatureVerifier.verify(
            coseSign1,
            publicKey,
            payload,
            VerificationError.INVALID_DEVICE_SIGNATURE
        )
    }
}
