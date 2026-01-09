package uk.gov.onelogin.sharing.verifier.verify

import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyCredentialViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()
    private lateinit var viewModel: VerifyCredentialViewModel

    @Before
    fun setup() {
        viewModel = VerifyCredentialViewModel(
            bluetoothStateMonitor = bluetoothStateMonitor,
            logger = logger
        )
    }

    @Test
    fun `initial state is idle`() {
        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.Idle
        )
    }

    @Test
    fun `starts observing bluetooth changes on initialisation`() {
        assert(bluetoothStateMonitor.startCalls == 1)
    }

    @Test
    fun `stops observing bluetooth changes onCleared`() {
        viewModel.onCleared()

        assert(bluetoothStateMonitor.stopCalls == 1)
    }

    @Test
    fun `preconditions are met when Bluetooth status changes to ON and permissions granted`() {
        viewModel.onPermissionsChanged(true)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.Met
        )
    }

    @Test
    fun `preconditions are not met when Bluetooth status changes to OFF`() {
        viewModel.onPermissionsChanged(true)
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothDisabled
        )
    }

    @Test
    fun `preconditions are not met when Permissions not granted`() {
        viewModel.onPermissionsChanged(false)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothAccessDenied
        )
    }

    @Test
    fun `emits NavigateToScanner event when preconditions are Met`() = runTest {
        viewModel.events.test {
            viewModel.onPermissionsChanged(true)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)

            advanceUntilIdle()

            assertEquals(VerifyCredentialEvents.NavigateToScanner, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateToScanner is emitted only once`() = runTest {
        viewModel.events.test {
            viewModel.onPermissionsChanged(true)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            advanceUntilIdle()

            assertEquals(VerifyCredentialEvents.NavigateToScanner, awaitItem())

            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            viewModel.onPermissionsChanged(true)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
