package uk.gov.onelogin.sharing.verifier.session

sealed interface VerifierSessionState {
    data object Idle : VerifierSessionState
    data object Starting : VerifierSessionState
    data object ServiceDiscovered : VerifierSessionState
    data object Connecting : VerifierSessionState
    data class Connected(val address: String) : VerifierSessionState
    data object Disconnected : VerifierSessionState
    data object Stopped : VerifierSessionState
    data class Error(val message: String) : VerifierSessionState
}
