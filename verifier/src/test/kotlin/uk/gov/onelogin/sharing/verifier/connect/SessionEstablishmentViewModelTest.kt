package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerFailure
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.scanner.DummyBluetoothScanner
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.UUIDExtensions.toBytes
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationale
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.security.DecoderStub
import uk.gov.onelogin.sharing.security.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasBase64EncodedEngagement
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasBluetoothDisabled
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasBluetoothEnabled
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateMatchers.hasDeviceEngagementDto
import uk.gov.onelogin.sharing.verifier.connect.SessionEstablishmentViewModelMatchers.hasUiState
import uk.gov.onelogin.sharing.verifier.connect.parameters.BluetoothStatusesToEnabledFlag
import uk.gov.onelogin.sharing.verifier.connect.parameters.EncodedEngagementToState
import uk.gov.onelogin.sharing.verifier.connect.parameters.PermissionsToLogMessages
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.undecodeableBarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession
import uk.gov.onelogin.sharing.verifier.session.VerifierSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = true)
    val scanner = FakeAndroidBluetoothScanner()
    val logger = SystemLogger()
    val fakeBluetoothStateMonitor = FakeBluetoothStateMonitor()
    val fakeVerifierSession = FakeVerifierSession()

    lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(scanner: BluetoothScanner) = SessionEstablishmentViewModel(
        bluetoothAdapterProvider = bluetoothAdapterProvider,
        scanner = scanner,
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = logger,
        bluetoothStatusMonitor = fakeBluetoothStateMonitor,
        verifierSessionFactory = { fakeVerifierSession }
    )

    @Test
    fun `init sets isBluetoothEnabled from adapter provider`() {
        viewModel = createViewModel(scanner)
        bluetoothAdapterProvider.setEnabled(false)
        assertEquals(true, viewModel.uiState.value.isBluetoothEnabled)
    }

    @Test
    fun `scanForDevice calls scanner with provided uuid`() = runTest {
        val uuid = byteArrayOf(0x01, 0x02, 0x03)
        viewModel = createViewModel(scanner)
        viewModel.updatePermissions(true)
        viewModel.scanForDevice(uuid)

        assertEquals(1, scanner.scanCalls)
        assertArrayEquals(uuid, scanner.lastUuid)
    }

    @Test
    fun `scanForDevice handles DeviceFound ScanEvent and logs it`() = runTest {
        val bluetoothDevice = mockk<BluetoothDevice>()
        every { bluetoothDevice.address } returns DEVICE_ADDRESS

        val scanner = BluetoothScanner.of(ScanEvent.DeviceFound(bluetoothDevice))

        val viewModel = createViewModel(scanner)

        viewModel.updatePermissions(true)

        val uuid = UUID.randomUUID()

        viewModel.scanForDevice(uuid.toByteArray())
        runCurrent()

        val logMessage = logger[0].message
        assert(logMessage.contains("Bluetooth device found"))
        assert(logMessage.contains(bluetoothDevice.address))
    }

    @Test
    fun `scanForDevice handles ScanFailure ScanEvent and logs it`() = runTest {
        val scanFailure = ScannerFailure.ALREADY_STARTED_SCANNING

        val scanner = BluetoothScanner.of(
            ScanEvent.ScanFailed(scanFailure)
        )

        val viewModel = createViewModel(scanner)

        viewModel.updatePermissions(true)

        viewModel.scanForDevice(byteArrayOf(0x01, 0x02, 0x03))
        runCurrent()

        val logMessage = logger[0].message
        assert(logMessage.contains("Scan failed"))
        assert(logMessage.contains(scanFailure.name))
    }

    @Test
    fun `stopScanning logs and cancels an active scan job`() = runTest {
        var flowClosed = false

        val scanner = BluetoothScanner.from(
            callbackFlow {
                awaitClose { flowClosed = true }
            }
        )

        val viewModel = createViewModel(scanner)

        viewModel.updatePermissions(true)
        viewModel.scanForDevice(byteArrayOf(0x01))
        runCurrent()

        viewModel.stopScanning()
        runCurrent()

        assertTrue(
            "Expected scan flow to be closed after cancel",
            flowClosed
        )
    }

    @Test
    fun `scanForDevice times out when no results emitted`() = runTest {
        val scanner = BluetoothScanner.from(
            callbackFlow {
                awaitCancellation()
            }
        )

        val viewModel = createViewModel(scanner)
        viewModel.updatePermissions(true)

        viewModel.scanForDevice(byteArrayOf(0x01, 0x02, 0x03))

        runCurrent()

        advanceTimeBy(SessionEstablishmentViewModel.SCAN_PERIOD)
        advanceUntilIdle()

        val logMessage = logger[0].message
        assert(logMessage.contains("TimeoutCancellationException:"))
    }

    @Test
    fun `scanForDevice on ScanEvent ScanFailure sets showErrorScreen true`() = runTest {
        val scanFailure = ScannerFailure.ALREADY_STARTED_SCANNING

        val scanner = BluetoothScanner.of(
            ScanEvent.ScanFailed(scanFailure)
        )

        val viewModel = createViewModel(scanner)

        viewModel.updatePermissions(true)

        viewModel.scanForDevice(byteArrayOf(0x01, 0x02, 0x03))
        runCurrent()

        assertEquals(
            ConnectWithHolderDeviceError.GenericError,
            viewModel.uiState.value.showErrorScreen
        )
    }

    @Test
    fun `Connecting to invalid configuration emits an error state to the UI`() = runTest {
        fakeVerifierSession.updateState(
            VerifierSessionState.Invalid
        )

        val viewModel = createViewModel(DummyBluetoothScanner)

        viewModel.connect(mockk(), UUID.randomUUID().toBytes())
        runCurrent()

        assertEquals(
            ConnectWithHolderDeviceError.BluetoothConfigurationError,
            viewModel.uiState.value.showErrorScreen
        )
    }

    @Test
    fun `should update hasRequestPermissions`() {
        viewModel = createViewModel(scanner)
        viewModel.updateHasRequestPermissions(true)
        assertEquals(true, viewModel.uiState.value.hasRequestedPermissions)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    @TestParameters(valuesProvider = PermissionsToLogMessages::class)
    fun `Permission updates are logged`(
        input: FakeMultiplePermissionsState,
        expectedMessage: String
    ) {
        viewModel = createViewModel(scanner)
        viewModel.update(input)

        assertTrue(
            "Couldn't find expected message in logger: $logger",
            expectedMessage in logger
        )
    }

    @Test
    @TestParameters(valuesProvider = BluetoothStatusesToEnabledFlag::class)
    fun `Bluetooth status maps to Bluetooth enablement flag`(
        status: BluetoothStatus,
        assertion: Matcher<ConnectWithHolderDeviceState>
    ) = runTest {
        viewModel = createViewModel(scanner)

        fakeBluetoothStateMonitor.emit(status)

        assertThat(
            viewModel,
            hasUiState(assertion)
        )
    }

    @Test
    @TestParameters(valuesProvider = EncodedEngagementToState::class)
    fun `Updating encoded data affects the UI state`(
        input: String,
        assertion: Matcher<ConnectWithHolderDeviceState>
    ) = runTest {
        viewModel = createViewModel(scanner)

        viewModel.update(input)

        assertThat(
            viewModel,
            hasUiState(assertion)
        )
    }
}
