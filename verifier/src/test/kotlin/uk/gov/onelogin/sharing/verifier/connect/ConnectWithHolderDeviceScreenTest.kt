package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    private var initialVerifierState: VerifierSessionState = VerifierSessionState.Connecting

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialVerifierState)
        )
    }

    private val testViewModel by lazy {
        SessionEstablishmentViewModel(
            logger = logger,
            orchestrator = orchestrator
        )
    }

    private val logger = SystemLogger()

    @Test
    fun `Shows a progress spinner whilst connecting with external device`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(testViewModel)
        composeTestRule.run {
            setContent { Render() }

            assertPlaceholderTextDoesNotExist()
        }
    }

    @Test
    fun `Has placeholder text for session states other than connecting`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialVerifierState = VerifierSessionState.NotStarted
        monitor(testViewModel)

        composeTestRule.run {
            setContent { Render() }

            assertPlaceholderTextExists()
        }
    }

    @Test
    fun `Session error states invoke the 'onConnectionError' lambda`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialVerifierState = VerifierSessionState.Complete.Failed(
            SessionError(
                "",
                Exception()
            )
        )

        composeTestRule.run {
            setContent { Render() }

            assertBluetoothSessionError(not(nullValue(BluetoothSessionError::class.java)))
        }
    }

    @Composable
    private fun Render() {
        ConnectWithHolderDeviceScreen(
            viewModel = testViewModel,
            onConnectionError = composeTestRule::updateOnConnectionError
        )
    }

    private fun TestScope.monitor(viewModel: SessionEstablishmentViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
        backgroundScope.launch { viewModel.navEvents.collect { } }
    }
}
