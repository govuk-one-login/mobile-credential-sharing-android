package uk.gov.onelogin.sharing.verification.document

import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

/**
 * Verifies that the SHA-256 hash of each issuer-signed item matches the corresponding digest
 * in the MSO value digests map.
 *
 * @throws VerificationResult.Failure
 */
fun interface DigestVerifier {
    fun verify(document: VerifiableDocument, mso: MobileSecurityObject)
}
