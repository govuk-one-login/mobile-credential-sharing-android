package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod

interface TrustVerifier {
    /**
     * @throws uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult.Failure When [data] cannot be verified, with one of the proceeding
     * errors:
     * - [uk.gov.onelogin.sharing.verification.format.document.result.VerificationError.MALFORMED_ISSUER_AUTH]
     * - [uk.gov.onelogin.sharing.verification.format.document.result.VerificationError.INVALID_ISSUER_SIGNATURE]
     * - [uk.gov.onelogin.sharing.verification.format.document.result.VerificationError.UNTRUSTED_CERTIFICATE]
     */
    fun verifyCOSESign1(
        data: ByteArray,
        trustedRoot: X509Certificate
    ): Pair<CertificateValidityPeriod, ByteArray>

    fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray)
}