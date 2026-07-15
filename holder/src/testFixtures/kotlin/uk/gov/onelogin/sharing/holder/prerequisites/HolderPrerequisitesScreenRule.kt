package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag

class HolderPrerequisitesScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule()
) : ComposeContentTestRule by composeTestRule {

    fun assertProgressIndicatorIsDisplayed() = onNodeWithTag(
        "progressIndicator"
    ).assertIsDisplayed()
}
