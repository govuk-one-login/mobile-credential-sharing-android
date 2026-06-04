package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

interface TrustVerifier {
    /**
     * Decodes the COSE_Sign1 structure, extracts and orders the x5chain, and returns
     * leaf certificate values and the MSO payload.
     *
     * @throws VerificationResult.Failure When [data] cannot be verified, with one of the proceeding
     * errors:
     * - [VerificationError.MALFORMED_ISSUER_AUTH]
     * - [VerificationError.INVALID_ISSUER_SIGNATURE]
     * - [VerificationError.UNTRUSTED_CERTIFICATE]
     */
    fun verifyCOSESign1(data: ByteArray, trustedRoot: X509Certificate): IssuerAuthResult

    fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray)
}
