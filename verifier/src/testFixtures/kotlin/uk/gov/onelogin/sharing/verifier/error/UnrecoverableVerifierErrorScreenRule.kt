package uk.gov.onelogin.sharing.verifier.error

import androidx.compose.ui.test.junit4.ComposeContentTestRule

class UnrecoverableVerifierErrorScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    var hasExitedJourney: Boolean = false
        private set

    fun assertHasExitedJourney() = waitUntil(
        "Hasn't called the 'onExitJourney' lambda!"
    ) { hasExitedJourney }

    fun updateHasExitedJourney(hasExitedJourney: Boolean = true) {
        this.hasExitedJourney = hasExitedJourney
    }
}
