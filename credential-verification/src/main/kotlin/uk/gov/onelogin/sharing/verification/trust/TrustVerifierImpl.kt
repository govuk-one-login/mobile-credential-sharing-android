package uk.gov.onelogin.sharing.verification.trust

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@ContributesBinding(CredentialVerificationScope::class)
class TrustVerifierImpl : TrustVerifier {
    override fun verifyCOSESign1(
        data: ByteArray,
        trustedRoot: X509Certificate
    ): Pair<CertificateValidityPeriod, ByteArray> =
        throw VerificationResult.Failure(VerificationError.MALFORMED_ISSUER_AUTH)

    override fun verifyCOSESign1(
        coseData: ByteArray,
        publicKey: ECPublicKey,
        payload: ByteArray
    ): Unit = throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

    internal fun decodeCOSESign1(): Unit = error("This function isn't implemented yet")
    internal fun verifyCertificateChain(): Unit = error("This function isn't implemented yet")
}
