package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HolderWelcomeTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "holderWelcomeText"

    @Test
    fun showsWelcomeText() = runTest {
        composeTestRule.setContent {
            HolderWelcomeText(
                modifier = Modifier.Companion.testTag(testTag)
            )
        }

        composeTestRule.onNodeWithTag(testTag)
            .assertIsDisplayed()
            .assertTextEquals(HolderWelcomeTexts.HOLDER_WELCOME_TEXT)
    }
}
