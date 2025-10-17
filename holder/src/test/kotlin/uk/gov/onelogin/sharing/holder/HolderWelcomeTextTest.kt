package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.HolderWelcomeTexts.HOLDER_WELCOME_TEXT

@RunWith(AndroidJUnit4::class)
class HolderWelcomeTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "holderWelcomeText"

    @Test
    fun showsWelcomeText() {
        composeTestRule.setContent {
            HolderWelcomeText(
                modifier = Modifier.testTag(testTag)
            )
        }

        composeTestRule.onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertTextEquals(HOLDER_WELCOME_TEXT)
    }
}