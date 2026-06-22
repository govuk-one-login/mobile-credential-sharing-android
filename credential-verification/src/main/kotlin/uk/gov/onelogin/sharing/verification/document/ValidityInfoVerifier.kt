package uk.gov.onelogin.sharing.verification.document

import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

/**
 * Verifies MSO validityInfo timestamps against the current time and the leaf certificate's
 * validity period.
 *
 * @throws VerificationResult.Failure
 */
fun interface ValidityInfoVerifier {
    fun verify(validityPeriod: CertificateValidityPeriod, validityInfo: ValidityInfo)
}
