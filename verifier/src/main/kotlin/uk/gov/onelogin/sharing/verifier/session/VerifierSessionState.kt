package uk.gov.onelogin.sharing.verifier.session

sealed interface VerifierSessionState {
    data object Idle : VerifierSessionState
    data object Verifying : VerifierSessionState
    data object ServiceDiscovered : VerifierSessionState
    data object VerifyingStopped : VerifierSessionState
    data class Error(val message: String) : VerifierSessionState
}
