package uk.gov.onelogin.sharing.verifier

sealed class VerifierNavigationEvents {
    data object NavigateToDiagnostic : VerifierNavigationEvents()
    data class NavigateToInvalidScreen(val qrCode: String) : VerifierNavigationEvents()
}
