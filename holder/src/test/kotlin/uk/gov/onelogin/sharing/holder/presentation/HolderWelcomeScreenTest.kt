package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.instanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(composeTestRule = createComposeRule())

    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        "This is a unit test"
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            dispatcher = dispatcherRule.testDispatcher,
            logger = SystemLogger(),
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Shows QR code whilst presenting engagement`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { Render() }
            assertWelcomeTextIsDisplayed()
            assertQrCodeIsDisplayed()
        }
    }

    @Test
    fun `Calls onAwaitingConsent lambda when session state changes`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialHolderState = HolderSessionState.AwaitingUserConsent(
            DeviceRequest(
                "",
                listOf()
            )
        )
        composeTestRule.run {
            setContent { Render() }
            assertOnAwaitingUserConsentIsCalled()
        }
    }

    @Test
    fun `Calls onConnectionError lambda when session state changes`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        val exception = BluetoothDisconnectedException("This is an exception", Exception())
        initialHolderState = HolderSessionState.Complete.Failed(
            SessionError(
                "This is a unit test",
                exception
            )
        )

        composeTestRule.run {
            setContent { Render() }
            assertOnConnectionError(
                instanceOf(BluetoothSessionError.BluetoothConnectionError::class.java)
            )
        }
    }

    @Test
    fun `Calls onGenericError lambda when session state changes`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        val exception = Exception("This is an exception")
        initialHolderState = HolderSessionState.Complete.Failed(
            SessionError(
                "This is a unit test",
                exception
            )
        )

        composeTestRule.run {
            setContent { Render() }
            assertOnGenericErrorIsCalled()
        }
    }

    @Test
    fun `Preview only shows QR content`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent { HolderWelcomeScreenPreview() }
            assertWelcomeTextIsDisplayed()
            assertQrCodeIsDisplayed()
        }

    }

    @Composable
    fun Render() {
        HolderWelcomeScreen(
            viewModel = viewModel,
            onAwaitingUserConsent = composeTestRule::callOnAwaitingUserConsent,
            onConnectionError = composeTestRule::callOnConnectionError,
            onGenericError = composeTestRule::callOnGenericError,
        )
    }
}