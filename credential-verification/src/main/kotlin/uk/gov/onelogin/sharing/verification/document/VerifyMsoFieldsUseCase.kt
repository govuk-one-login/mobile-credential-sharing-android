package uk.gov.onelogin.sharing.verification.document

import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

/**
 * Verifies MSO fields against the document and issuer certificate attributes.
 *
 * @throws VerificationResult.Failure
 */
fun interface VerifyMsoFieldsUseCase {
    fun verify(
        document: VerifiableDocument,
        mso: MobileSecurityObject,
        issuerAuthResult: IssuerAuthResult
    )
}
