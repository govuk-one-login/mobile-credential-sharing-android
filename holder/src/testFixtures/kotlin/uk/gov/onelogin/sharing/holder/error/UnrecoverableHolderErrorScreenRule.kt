package uk.gov.onelogin.sharing.holder.error

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan

class UnrecoverableHolderErrorScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    var hasExitedJourney: Boolean = false
        private set

    var hasExitedJourneyCount: Int = 0
        private set

    fun assertHasExitedJourney(): Unit = waitUntil(
        "Hasn't called the 'onExitJourney' lambda!"
    ) { hasExitedJourney }

    fun assertHasExitedJourneyCount(expectedCount: Matcher<in Int> = greaterThan(0)): Unit =
        waitUntil(
            "Expected count doesn't match! Actual count: $hasExitedJourneyCount"
        ) {
            expectedCount.matches(hasExitedJourneyCount)
        }

    fun updateHasExitedJourney(hasExitedJourney: Boolean = true) {
        this.hasExitedJourney = hasExitedJourney
        this.hasExitedJourneyCount++
    }
}
