package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriod

interface TrustVerifier {
    /**
     * @throws uk.gov.onelogin.sharing.verification.document.result.VerificationResult.Failure
     */
    fun verifyCOSESign1(
        data: ByteArray,
        trustedRoot: X509Certificate
    ): Pair<CertificateValidityPeriod, ByteArray>

    fun verifyCOSESign1(
        coseData: ByteArray,
        publicKey: ECPublicKey,
        payload: ByteArray
    )
}
