package uk.gov.onelogin.sharing.verifier.verify

import app.cash.turbine.test
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.allPermissionsGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedCameraGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationaleCameraGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.cameraPermissionDenied
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPermissionsApi::class)
class VerifyCredentialViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()
    private lateinit var viewModel: VerifyCredentialViewModel
    private val fakeOrchestrator = FakeOrchestrator()

    @Before
    fun setup() {
        viewModel = VerifyCredentialViewModel(
            bluetoothStateMonitor = bluetoothStateMonitor,
            logger = logger,
            orchestrator = fakeOrchestrator
        )
    }

    @Test
    fun `orchestrator calls start on init`() {
        assertEquals(1, fakeOrchestrator.startCount)
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
        viewModel.onPermissionsChanged(allPermissionsGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.Met
        )

        assert(logger.contains("All required Bluetooth permissions have been granted"))
    }

    @Test
    fun `preconditions are not met when Bluetooth status changes to OFF`() {
        viewModel.onPermissionsChanged(allPermissionsGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothDisabled
        )
    }

    @Test
    fun `preconditions are not met when Bluetooth Permissions denied`() {
        viewModel.onPermissionsChanged(bluetoothPermissionsDeniedWithRationaleCameraGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothAccessDenied
        )

        assert(logger.contains("Bluetooth permissions were denied"))
    }

    @Test
    fun `preconditions are not met when Bluetooth Permissions permanently denied`() {
        viewModel.onPermissionsChanged(bluetoothPermissionsDeniedCameraGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothAccessDenied
        )

        assert(logger.contains("Bluetooth permissions were permanently denied"))
    }

    @Test
    fun `preconditions are not met when Camera Permission denied`() {
        viewModel.onPermissionsChanged(cameraPermissionDenied)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.CameraAccessDenied
        )
    }

    @Test
    fun `emits NavigateToScanner event when preconditions are Met`() = runTest {
        viewModel.events.test {
            viewModel.onPermissionsChanged(allPermissionsGranted)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)

            advanceUntilIdle()

            assertEquals(VerifyCredentialEvents.NavigateToScanner, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `NavigateToScanner is emitted only once`() = runTest {
        viewModel.events.test {
            viewModel.onPermissionsChanged(allPermissionsGranted)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            advanceUntilIdle()

            assertEquals(VerifyCredentialEvents.NavigateToScanner, awaitItem())

            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            viewModel.onPermissionsChanged(allPermissionsGranted)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)
            advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
