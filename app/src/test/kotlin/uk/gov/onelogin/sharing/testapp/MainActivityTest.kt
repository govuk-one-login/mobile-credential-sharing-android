package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun displaysWelcomeTextAndQrCode() {
        composeTestRule.onNodeWithText(HOLDER_WELCOME_TEXT)
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
            .assertIsDisplayed()
    }
}
