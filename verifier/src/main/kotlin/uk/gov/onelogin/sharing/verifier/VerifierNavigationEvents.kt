package uk.gov.onelogin.sharing.verifier

sealed class VerifierNavigationEvents {
    data class NavigateToDiagnostic(val qrCode: String) : VerifierNavigationEvents()
    data class NavigateToInvalidScreen(val qrCode: String) : VerifierNavigationEvents()
}
