package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag

class VerifierTestAppJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    fun assertPrerequisitesProgressIndicatorIsDisplayed() =
        onNodeWithTag("progressIndicator").assertIsDisplayed()
}
