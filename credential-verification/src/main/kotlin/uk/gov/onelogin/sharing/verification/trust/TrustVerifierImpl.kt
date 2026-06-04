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

@ContributesBinding(CredentialVerificationScope::class)
class TrustVerifierImpl : TrustVerifier {

    @OptIn(ExperimentalTime::class)
    override fun verifyCOSESign1(
        data: ByteArray,
        trustedRoot: X509Certificate
    ): IssuerAuthResult {
        val coseSign1 = CoseSign1Decoder.decode(data)
        val x5chain = CoseSign1Decoder.extractX5Chain(coseSign1)

        val certFactory = CertificateFactory.getInstance("X.509")
        val certs = x5chain.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        val ordered = orderCertificates(certs)
        val leaf = ordered.first()

        val validityPeriod = CertificateValidityPeriod(
            notBefore = leaf.notBefore.toInstant().toKotlinInstant(),
            notAfter = leaf.notAfter.toInstant().toKotlinInstant()
        )

        return IssuerAuthResult(
            certificateValidityPeriod = validityPeriod,
            msoPayload = coseSign1.payload
                ?: throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH),
            subjectCountry = "C",
            subjectState = "ST"
        )
    }

    override fun verifyCOSESign1(
        coseData: ByteArray,
        publicKey: ECPublicKey,
        payload: ByteArray
    ): Unit = throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

    internal fun orderCertificates(certs: List<X509Certificate>): List<X509Certificate> {
        if (certs.size <= 1) return certs

        val skiToCert = mutableMapOf<String, X509Certificate>()
        certs.forEach { cert ->
            cert.subjectKeyIdentifierHex()?.let { skiToCert[it] = cert }
        }

        val leaf = certs.find { cert ->
            val aki = cert.authorityKeyIdentifierHex() ?: return@find true
            !skiToCert.containsKey(aki)
        } ?: certs.first()

        val ordered = mutableListOf(leaf)
        var current = leaf
        while (ordered.size < certs.size) {
            val aki = current.authorityKeyIdentifierHex() ?: break
            val parent = skiToCert[aki]
            if (parent == null || ordered.contains(parent)) break
            ordered.add(parent)
            current = parent
        }

        return ordered
    }
}
