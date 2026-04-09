package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.ComposeContentTestRule

class VerifyCredentialRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {

    var hasNavigatedToPreflight: Boolean = false
        private set
    var hasNavigatedToScanner: Boolean = false
        private set
    var hasUnrecoverableError: Boolean = false
        private set

    fun assertHasNavigatedToPreflight() = waitUntil(
        "UI hadn't called the 'onNavigateToPreflight' lambda to update state!"
    ) { hasNavigatedToPreflight }
    fun assertHasNavigatedToScanner() = waitUntil(
        "UI hadn't called the 'onNavigateToScanner' lambda to update state!"
    ) { hasNavigatedToScanner }
    fun assertHasUnrecoverableError() = waitUntil(
        "UI hadn't called the 'onUnrecoverableError' lambda to update state!"
    ) { hasUnrecoverableError }

    fun updateHasNavigatedToPreflight(hasNavigatedToPreflight: Boolean = true) {
        this.hasNavigatedToPreflight = hasNavigatedToPreflight
    }
    fun updateHasNavigatedToScanner(hasNavigatedToScanner: Boolean = true) {
        this.hasNavigatedToScanner = hasNavigatedToScanner
    }
    fun updateHasUnrecoverableError(hasUnrecoverableError: Boolean = true) {
        this.hasUnrecoverableError = hasUnrecoverableError
    }
}
