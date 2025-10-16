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

@RunWith(AndroidJUnit4::class)
class MainActivityScreenTest {

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
            .assertTextEquals("Welcome to GOV.UK Wallet Sharing")
    }

    @Test
    fun showsQrCode() {
        composeTestRule.setContent {
            QrCodeImage(
                modifier = Modifier.testTag("qrCode"),
                data = "https://www.gov.uk",
                size = 800
            )
        }
        composeTestRule
            .onNode(hasContentDescription("QR code to gov.uk"))
            .assertIsDisplayed()
    }

    @Test
    fun showsNoQrCodeWhenDataIsEmpty() {
        composeTestRule.setContent {
            QrCodeImage(
                modifier = Modifier.testTag("qrCode"),
                data = "",
                size = 800
            )
        }
        composeTestRule
            .onNode(hasContentDescription("QR code to gov.uk"))
            .assertDoesNotExist()
    }
}