package uk.gov.onelogin.sharing.verifier.verify

import kotlin.test.Test
import org.junit.Before
import org.junit.Rule
import uk.gov.logging.testdouble.LogEntry
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.logger.logTag

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
    fun `initial state is bluetooth disabled`() {
        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothDisabled
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
    fun `preconditions are met when Bluetooth status changes to ON`() {
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            logger.contains(
                LogEntry.Message(
                    viewModel.logTag,
                    "User enabled bluetooth via prompt"
                )
            )
        )

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.Met
        )
    }

    @Test
    fun `preconditions are met when Bluetooth status changes to OFF`() {
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        assert(
            logger.contains(
                LogEntry.Message(
                    viewModel.logTag,
                    "User cancelled bluetooth prompt"
                )
            )
        )

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothDisabled
        )
    }
}
