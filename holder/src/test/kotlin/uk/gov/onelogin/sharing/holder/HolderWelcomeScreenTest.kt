package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeContentState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenContent
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
    fun `initial content is displayed`() {
        val contentState = HolderWelcomeContentState(
            errorMessage = null,
            advertiserState = AdvertiserState.Idle,
            uuid = "123",
            qrCodeData = "some-qr-data"
        )
        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = contentState,
                onStartClick = { },
                onStopClick = { },
                onShowError = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Bluetooth Advertising")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Status: Idle")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Start")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()

        composeTestRule
            .onNodeWithText("Stop")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun `start and stop buttons are visible and enabled when advertising`() {
        var startClicked = 0
        var stopClicked = 0
        val contentState = HolderWelcomeContentState(
            errorMessage = null,
            advertiserState = AdvertiserState.Started,
            uuid = "123",
            qrCodeData = "some-qr-data"
        )

        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = contentState,
                onStartClick = { startClicked++ },
                onStopClick = { stopClicked++ },
                onShowError = {}
            )
        }

        val startButton = composeTestRule
            .onNodeWithText("Start")
            .performScrollTo()
            .assertIsEnabled()

        val stopButton = composeTestRule
            .onNodeWithText("Stop")
            .performScrollTo()
            .assertIsEnabled()

        startButton.performClick()
        stopButton.performClick()

        assert(startClicked == 1)
        assert(stopClicked == 1)
    }

    @Test
    fun `shows uuid when advertising started`() {
        val uuid = "11111111-2222-3333-4444-555555555555"
        val contentState = HolderWelcomeContentState(
            errorMessage = null,
            advertiserState = AdvertiserState.Started,
            uuid = uuid,
            qrCodeData = "some-qr-data"
        )
        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = contentState,
                onStartClick = {},
                onStopClick = {},
                onShowError = {}
            )
        }

        composeTestRule.onNodeWithText("Status: Started").assertIsDisplayed()

        composeTestRule.onNodeWithText("Stop").assertIsEnabled()

        composeTestRule.onNodeWithText("UUID: $uuid")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `calls OnErrorShown when errorMessage is non-null`() {
        var errorShown = false
        val contentState = HolderWelcomeContentState(
            errorMessage = "Something went wrong",
            advertiserState = AdvertiserState.Stopped,
            uuid = "123",
            qrCodeData = null
        )

        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = contentState,
                onStartClick = {},
                onStopClick = {},
                onShowError = { errorShown = true }
            )
        }

        composeTestRule.waitForIdle()

        assert(errorShown) {
            "onErrorShown should be called when errorMessage is non-null"
        }
    }
}
