package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError

@OptIn(ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = SystemLogger()

    private fun createViewModel(
        orchestrator: FakeOrchestrator = FakeOrchestrator(parser = FakeQrParser())
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        logger = logger,
        savedStateHandle = SavedStateHandle(),
        orchestrator = orchestrator,
        dispatcher = mainDispatcherRule.testDispatcher
    )

    @Test
    fun `updateBluetoothPermissions should update hasBluetoothPermissions`() {
        val viewModel = createViewModel()

        viewModel.updateBluetoothPermissions(true)

        assertEquals(true, viewModel.uiState.value.hasBluetoothPermissions)
    }

    @Test
    fun `bluetooth permissions granted initially and sets previouslyHadPermissions true`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.updateBluetoothPermissions(granted = true)

            val state = viewModel.uiState.value

            assertTrue(state.hasBluetoothPermissions!!)
            assertTrue(state.previouslyHadPermissions)
            assertFalse(state.showErrorScreen)
        }

    @Test
    fun `error should not be shown if permissions initially not granted on start up`() = runTest {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.previouslyHadPermissions)

        viewModel.updateBluetoothPermissions(granted = false)

        val state = viewModel.uiState.value

        assertFalse(state.hasBluetoothPermissions!!)
        assertFalse(state.previouslyHadPermissions)
        assertFalse(state.showErrorScreen)
    }

    @Test
    fun `when orchestrator state is PresentingEngagement, should set QR data to ui state`() =
        runTest {
            val orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(
                    HolderSessionState.PresentingEngagement(qrData = "fakeQrData")
                )
            )

            val viewModel = createViewModel(orchestrator = orchestrator)

            orchestrator.start()
            advanceUntilIdle()

            assertEquals("fakeQrData", viewModel.uiState.value.qrData)
        }

    @Test
    fun `emits BluetoothConnectionError when failure is BluetoothDisconnectedException`() =
        runTest {
            val orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(HolderSessionState.NotStarted)
            )
            val viewModel = createViewModel(orchestrator = orchestrator)
            advanceUntilIdle()

            viewModel.navEvents.test {
                orchestrator.initialHolderState.value = HolderSessionState.Complete.Failed(
                    SessionError(
                        message = "disconnected",
                        exception = BluetoothDisconnectedException(
                            "disconnected",
                            IllegalStateException("cause")
                        )
                    )
                )
                advanceUntilIdle()

                assertEquals(
                    BluetoothSessionError.BluetoothConnectionError,
                    (awaitItem() as HolderScreenEvents.NavigateToBluetoothError).error
                )
            }
        }

    @Test
    fun `emits NavigateToGenericError when failure is DeviceRequestDecodingException`() = runTest {
        val orchestrator = FakeOrchestrator(
            initialHolderState = MutableStateFlow(HolderSessionState.NotStarted)
        )
        val viewModel = createViewModel(orchestrator = orchestrator)
        advanceUntilIdle()

        viewModel.navEvents.test {
            orchestrator.initialHolderState.value = HolderSessionState.Complete.Failed(
                SessionError(
                    message = "CBOR decoding error",
                    exception = DeviceRequestDecodingException("CBOR decoding error")
                )
            )
            advanceUntilIdle()

            assertEquals(HolderScreenEvents.NavigateToGenericError, awaitItem())
        }
    }

    @Test
    fun `emits NavigateToGenericError when failure is not a specific exception`() = runTest {
        val orchestrator = FakeOrchestrator(
            initialHolderState = MutableStateFlow(HolderSessionState.NotStarted)
        )
        val viewModel = createViewModel(orchestrator = orchestrator)
        advanceUntilIdle()

        viewModel.navEvents.test {
            orchestrator.initialHolderState.value = HolderSessionState.Complete.Failed(
                SessionError(
                    message = "encryption failed",
                    exception = RuntimeException("encryption failed")
                )
            )
            advanceUntilIdle()

            assertEquals(HolderScreenEvents.NavigateToGenericError, awaitItem())
        }
    }
}
