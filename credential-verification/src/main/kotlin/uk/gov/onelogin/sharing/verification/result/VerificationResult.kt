package uk.gov.onelogin.sharing.verification.result

sealed interface VerificationResult {
    data object Success : VerificationResult
    data class Failure(val error: VerificationError) :
        Throwable(error.name),
        VerificationResult
}
