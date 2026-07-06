package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText

class AwaitingVerifierResolutionScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule()
) : ComposeContentTestRule by composeTestRule {

    fun assertTitleIsDisplayed() = onNodeWithText(
        "Details shared"
    ).assertIsDisplayed()
}
