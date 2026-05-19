package uk.gov.onelogin.sharing.verification

sealed interface VerificationResult {
    data object Success : VerificationResult
}
