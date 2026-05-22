package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.document.result.VerificationResult

interface TrustVerifier {
    /**
     * @throws VerificationResult.Failure When [data] cannot be verified, with one of the proceeding
     * errors:
     * - [VerificationError.MALFORMED_ISSUER_AUTH]
     * - [VerificationError.INVALID_ISSUER_SIGNATURE]
     * - [VerificationError.UNTRUSTED_CERTIFICATE]
     */
    fun verifyCOSESign1(
        data: ByteArray,
        trustedRoot: X509Certificate
    ): Pair<CertificateValidityPeriod, ByteArray>

    fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray)
}
