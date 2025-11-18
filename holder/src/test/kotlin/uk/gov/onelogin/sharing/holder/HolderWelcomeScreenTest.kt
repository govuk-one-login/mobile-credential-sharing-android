package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenContent
import uk.gov.onelogin.sharing.verifier.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.verifier.stubHolderWelcomeUiState

@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysWelcomeText() {
        composeTestRule.setContent {
            HolderWelcomeScreen()
        }

        composeTestRule
            .onNodeWithText(HOLDER_WELCOME_TEXT)
            .assertIsDisplayed()
    }

    @Test
    fun `initial content is displayed`() {
        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = stubHolderWelcomeUiState()
            )
        }

        composeTestRule
            .onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
            .assertIsDisplayed()
    }
}
