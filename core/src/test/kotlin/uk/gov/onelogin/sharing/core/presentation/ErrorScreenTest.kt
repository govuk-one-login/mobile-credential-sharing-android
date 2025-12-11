package uk.gov.onelogin.sharing.core.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayGenericErrorText() {
        composeTestRule.setContent {
            ErrorScreen(modifier = Modifier)
        }

        composeTestRule.onNodeWithText("An error has occurred").assertIsDisplayed()
    }

    @Test
    fun screenWithTextAnErrorHasOccurredShouldExist() {
        composeTestRule.setContent {
            ErrorScreen(modifier = Modifier)
        }
        composeTestRule.onNodeWithText("An error has occurred").assertExists()
    }
}
