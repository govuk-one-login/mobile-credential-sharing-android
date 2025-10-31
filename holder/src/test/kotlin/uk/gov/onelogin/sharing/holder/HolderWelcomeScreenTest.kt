package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC

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
    fun displaysQrCode() {
        composeTestRule.setContent {
            HolderWelcomeScreen()
        }

        composeTestRule.onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
            .assertIsDisplayed()
    }
}
