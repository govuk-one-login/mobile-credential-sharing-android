package uk.gov.onelogin.sharing.verification.trust

import dev.zacsweers.metro.ContributesBinding
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import kotlin.time.Clock
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

        val now = Clock.System.now()

        val validityPeriod = CertificateValidityPeriod(
            notBefore = now,
            notAfter = now
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
}
