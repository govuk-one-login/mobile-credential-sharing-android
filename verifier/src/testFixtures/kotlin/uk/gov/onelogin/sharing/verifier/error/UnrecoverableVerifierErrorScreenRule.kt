package uk.gov.onelogin.sharing.verifier.error

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan

class UnrecoverableVerifierErrorScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    private var hasExitedJourney: Boolean = false
    private var hasExitedJourneyCount = 0

    fun assertHasExitedJourney() = waitUntil(
        "Hasn't called the 'onExitJourney' lambda!"
    ) { hasExitedJourney }

    fun assertHasExitedJourneyCount(expectedCount: Matcher<in Int> = greaterThan(0)): Unit =
        waitUntil(
            "Unexpected exit journey call count! Actual: $hasExitedJourneyCount"
        ) { expectedCount.matches(hasExitedJourneyCount) }

    fun updateHasExitedJourney(hasExitedJourney: Boolean = true) {
        this.hasExitedJourney = hasExitedJourney
        this.hasExitedJourneyCount++
    }
}
