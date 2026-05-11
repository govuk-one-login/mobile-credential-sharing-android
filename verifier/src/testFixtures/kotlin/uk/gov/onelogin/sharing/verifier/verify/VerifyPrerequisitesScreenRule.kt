package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag

class VerifyPrerequisitesScreenRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {

    fun assertCircularProgressIndicatorIsDisplayed() = onNodeWithTag("progressIndicator")
        .assertIsDisplayed()
}
