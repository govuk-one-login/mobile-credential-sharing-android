package uk.gov.onelogin.sharing.security

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
import uk.gov.onelogin.sharing.security.SecurityWelcomeTexts.SECURITY_WELCOME_TEXT

@RunWith(AndroidJUnit4::class)
class SecurityWelcomeTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "securityWelcomeText"

    @Test
    fun showsWelcomeText() {
        composeTestRule.setContent {
            SecurityWelcomeText(
                modifier = Modifier.testTag(testTag)
            )
        }

        composeTestRule.onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertTextEquals(SECURITY_WELCOME_TEXT)
    }
}
