package uk.gov.onelogin.sharing.verifier.verify

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationale
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesViewModelExt.monitor

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPermissionsApi::class)
class VerifierPrerequisitesViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()
    private var sessionState = MutableStateFlow<VerifierSessionState>(
        VerifierSessionState.NotStarted
    )
    private val fakeOrchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = sessionState
        )
    }
    private val viewModel by lazy {
        VerifierPrerequisitesViewModel(
            bluetoothStateMonitor = bluetoothStateMonitor,
            logger = logger,
            orchestrator = fakeOrchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `orchestrator calls start on init`() = runTest(dispatcherRule.testDispatcher) {
        monitor(viewModel)
        assertEquals(1, fakeOrchestrator.startCount)
    }

    @Test
    fun `initial state is idle`() = runTest(dispatcherRule.testDispatcher) {
        monitor(viewModel)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.Idle
        )
    }

    @Test
    fun `starts observing bluetooth changes on initialisation`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        assert(bluetoothStateMonitor.startCalls == 1)
    }

    @Test
    fun `stops observing bluetooth changes onCleared`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        viewModel.onCleared()

        assert(bluetoothStateMonitor.stopCalls == 1)
    }

    @Test
    fun `preconditions are met when Bluetooth status changes to ON and permissions granted`() =
        runTest(dispatcherRule.testDispatcher) {
            monitor(viewModel)

            viewModel.onPermissionsChanged(bluetoothPermissionsGranted)
            bluetoothStateMonitor.emit(BluetoothStatus.ON)

            assert(
                viewModel.uiState.value.preconditionsState
                    is VerifyCredentialPreconditionsState.Met
            )

            assert(logger.contains("All required Bluetooth permissions have been granted"))
        }

    @Test
    fun `preconditions are not met when Bluetooth status changes to OFF`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        viewModel.onPermissionsChanged(bluetoothPermissionsGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothDisabled
        )
    }

    @Test
    fun `preconditions are not met when Permissions denied first time`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        viewModel.onPermissionsChanged(bluetoothPermissionsDeniedWithRationale)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothAccessDenied
        )

        assert(logger.contains("Bluetooth permissions were denied"))
    }

    @Test
    fun `preconditions are not met when Permissions permanently denied`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        viewModel.onPermissionsChanged(bluetoothPermissionsDenied)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        assert(
            viewModel.uiState.value.preconditionsState
                is VerifyCredentialPreconditionsState.BluetoothAccessDenied
        )

        assert(logger.contains("Bluetooth permissions were permanently denied"))
    }

    @Test
    fun `emits NavigateToScanner event when preconditions are Met`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        sessionState = MutableStateFlow(VerifierSessionState.ReadyToScan)
        monitor(viewModel)
        viewModel.onPermissionsChanged(bluetoothPermissionsGranted)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)
        advanceUntilIdle()

        assertEquals(VerifyCredentialEvents.NavigateToScanner, viewModel.events.value)
    }

    @Test
    fun `Emits NavigateToPreflight event when preconditions are met`() = runTest {
        sessionState = MutableStateFlow(preflightEmptyPermissions)
        monitor(viewModel)

        advanceUntilIdle()

        assertEquals(
            VerifyCredentialEvents.NavigateToPreflight,
            viewModel.events.value
        )
    }
}
