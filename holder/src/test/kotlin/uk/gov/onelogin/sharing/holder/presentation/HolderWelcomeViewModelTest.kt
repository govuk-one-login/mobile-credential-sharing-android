package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = SystemLogger()

    private fun createViewModel(
        orchestrator: FakeOrchestrator = FakeOrchestrator()
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
    fun `bluetooth permissions revoked and error screen shown`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateBluetoothPermissions(granted = true)
        assertTrue(viewModel.uiState.value.previouslyHadPermissions)

        viewModel.updateBluetoothPermissions(granted = false)

        val state = viewModel.uiState.value

        assertFalse(state.hasBluetoothPermissions!!)
        assertTrue(state.previouslyHadPermissions)
        assertTrue(state.showErrorScreen)
        assertEquals(
            "Bluetooth permissions were revoked during the session",
            state.errorMessage
        )
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
}
