package uk.gov.onelogin.sharing.verifier.scan.state

sealed class VerifierUiState {
    data object Loading : VerifierUiState()
    data object StartScanner : VerifierUiState()
}
