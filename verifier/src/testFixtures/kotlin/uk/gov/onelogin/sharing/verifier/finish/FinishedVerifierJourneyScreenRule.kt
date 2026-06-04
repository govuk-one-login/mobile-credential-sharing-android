package uk.gov.onelogin.sharing.verifier.finish

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class FinishedVerifierJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var hasExitedJourney: Boolean = false

    fun assertHasExitedJourney() = waitUntil { hasExitedJourney }

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
    }
}
