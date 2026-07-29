package uk.gov.onelogin.sharing.verifier.finish

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThan
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class FinishedVerifierJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var hasExitedJourney: Boolean = false
    private var hasExitedJourneyCount = 0

    fun assertHasExitedJourney() = waitUntil { hasExitedJourney }

    fun assertHasExitedJourneyCount(expectedCount: Matcher<in Int> = greaterThan(0)) = waitUntil(
        "Unexpected exit journey call count! Actual: $hasExitedJourneyCount"
    ) { expectedCount.matches(hasExitedJourneyCount) }

    fun assertDocumentIsDisplayed(document: VerifiableDocument) =
        onNodeWithText(document.toString(), substring = true)
            .assertExists()
            .assertIsDisplayed()

    fun performExitJourneyClick() = onNodeWithText("Exit journey", useUnmergedTree = true)
        .onParent()
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    fun updateHasExitedJourney(hasExitedJourney: Boolean = true) {
        this.hasExitedJourney = hasExitedJourney
        this.hasExitedJourneyCount++
    }
}
