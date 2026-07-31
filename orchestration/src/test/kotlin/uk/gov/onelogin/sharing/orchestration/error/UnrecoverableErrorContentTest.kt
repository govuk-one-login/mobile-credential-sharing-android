package uk.gov.onelogin.sharing.orchestration.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError

@RunWith(RobolectricTestParameterInjector::class)
class UnrecoverableErrorContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasExitedJourney: Boolean = false

    @Test
    fun `Tapping the exit journey button calls 'onExitJourney' lambda`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            onNodeWithText("Exit journey", useUnmergedTree = true).performClick()

            waitUntil { hasExitedJourney }
        }
    }

    @Test
    fun `Title matches the SessionErrorReason's simple name`(
        @TestParameter reason: SessionErrorReason = testValuesIn(reasons)
    ) = runTest {
        composeTestRule.run {
            setContent { Render(reason = reason) }

            onNodeWithText(reason::class.java.simpleName)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun `Error description matches the SessionError's message`(
        @TestParameter message: String = testValues(
            "This is a unit test",
            "An exception has occurred"
        )
    ) = runTest {
        composeTestRule.run {
            setContent { Render(message = message) }

            onNodeWithText(message)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Composable
    private fun Render(
        message: String = "This is a unit test",
        reason: SessionErrorReason = SessionErrorReason.UnrecoverableThrowable(Exception())
    ) {
        UnrecoverableErrorContent(
            failureState = SessionError(message, reason),
            onExitJourney = { hasExitedJourney = true }
        )
    }

    companion object {
        private val reasons: List<SessionErrorReason> = listOf(
            SessionErrorReason.CannotBuildSessionEstablishment,
            SessionErrorReason.CannotDecryptDeviceResponse,
            SessionErrorReason.CannotEncryptDeviceRequest,
            SessionErrorReason.CannotProcessEngagement("qrCode"),
            SessionErrorReason.CannotSendMessage,
            SessionErrorReason.DeviceRequestProcessingError(0u),
            SessionErrorReason.DocumentNotReturned,
            SessionErrorReason.InvalidBluetoothState(Exception()),
            SessionErrorReason.InvalidSessionDataPayload,
            SessionErrorReason.MissingCryptoContext,
            SessionErrorReason.PeerTermination,
            SessionErrorReason.ServiceUuidNotFound,
            SessionErrorReason.StatusError(0u),
            SessionErrorReason.UnrecoverablePrerequisite(),
            SessionErrorReason.UnrecoverableThrowable(Exception()),
            SessionErrorReason.UnsupportedQrCodeFormat("invalidQrData"),
            SessionErrorReason.UnverifiableDocument(VerificationError.INVALID_DOC_TYPE)
        )
    }
}
