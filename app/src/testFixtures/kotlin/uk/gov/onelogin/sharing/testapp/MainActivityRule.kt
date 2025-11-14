package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode

class MainActivityRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    fun performHolderTabClick() {
        onNodeWithText("Holder")
            .assertIsDisplayed()
            .performClick()
    }

    fun performVerifierTabClick() {
        onNodeWithText("Verifier")
            .assertIsDisplayed()
            .performClick()
    }

    fun performMenuItemClick(textToClick: String) {
        onNodeWithText("menuItems")
            .performScrollToNode(hasText(textToClick))
            .assertIsDisplayed()
            .performClick()
    }
}
