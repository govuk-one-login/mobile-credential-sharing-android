package uk.gov.onelogin.sharing.verifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.VerifierWelcomeTexts.VERIFIER_WELCOME_TEXT

@RunWith(AndroidJUnit4::class)
class VerifierWelcomeTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "verifierWelcomeText"

    @Test
    fun showsWelcomeText() {
        composeTestRule.setContent {
            VerifierWelcomeText(
                modifier = Modifier.testTag(testTag)
            )
        }

        composeTestRule.onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertTextEquals(VERIFIER_WELCOME_TEXT)
    }
}
