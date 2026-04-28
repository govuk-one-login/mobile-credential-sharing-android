package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick

class VerifierTestAppJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var hasClosedJourney: Boolean = false

    fun assertHasClosedJourney() = waitUntil { hasClosedJourney }

    fun performCloseJourneyClick() = onNodeWithContentDescription(
        "Close",
        useUnmergedTree = true
    ).performClick()

    fun updateHasClosedJourney(hasClosedJourney: Boolean = true) {
        this.hasClosedJourney = hasClosedJourney
    }
}
