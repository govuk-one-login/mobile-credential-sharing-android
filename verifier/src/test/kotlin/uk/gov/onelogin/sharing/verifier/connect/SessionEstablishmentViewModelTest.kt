package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothCentralFactory
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerFailure
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.central.FakeGattClientManager
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateDeniedWithRationale
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateGranted
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession

@OptIn(ExperimentalCoroutinesApi::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = true)
    val scanner = FakeAndroidBluetoothScanner()
    val logger = SystemLogger()
    val fakeVerifierSession = FakeVerifierSession()

    lateinit var viewModel: SessionEstablishmentViewModel

    private fun createViewModel(scanner: BluetoothScanner) = SessionEstablishmentViewModel(
        bluetoothAdapterProvider = bluetoothAdapterProvider,
        scanner = scanner,
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = logger,
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

        val scanner = BluetoothScanner {
            flowOf(ScanEvent.DeviceFound(bluetoothDevice))
        }

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

        val scanner = object : BluetoothScanner {
            override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> = flowOf(
                ScanEvent.ScanFailed(scanFailure)
            )
        }

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

        val scanner = object : BluetoothScanner {
            override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> = callbackFlow {
                awaitClose { flowClosed = true }
            }
        }

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
        val scanner = object : BluetoothScanner {
            override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> = callbackFlow {
                awaitCancellation()
            }
        }

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

        val scanner = object : BluetoothScanner {
            override fun scan(serviceUuid: ByteArray): Flow<ScanEvent> = flowOf(
                ScanEvent.ScanFailed(scanFailure)
            )
        }

        val viewModel = createViewModel(scanner)

        viewModel.updatePermissions(true)

        viewModel.scanForDevice(byteArrayOf(0x01, 0x02, 0x03))
        runCurrent()

        assertTrue(viewModel.uiState.value.showErrorScreen)
    }

    @Test
    fun `should update hasRequestPermissions`() {
        viewModel = createViewModel(scanner)
        viewModel.updateHasRequestPermissions(true)
        assertEquals(true, viewModel.uiState.value.hasRequestedPermissions)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should log permission granted when user allows all permissions`() {
        viewModel = createViewModel(scanner)
        viewModel.permissionLogger(fakePermissionStateGranted)

        val logMessage = logger[0].message
        assert(logMessage.contains("All required Bluetooth permissions have been granted"))
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should log permission permanently denied when user denies permissions completely`() {
        viewModel = createViewModel(scanner)
        viewModel.permissionLogger(fakePermissionStateDenied)

        val logMessage = logger[0].message
        assert(logMessage.contains("Bluetooth permissions were permanently denied"))
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should log permissions denied when user denied first time`() {
        viewModel = createViewModel(scanner)
        viewModel.permissionLogger(fakePermissionStateDeniedWithRationale)

        val logMessage = logger[0].message
        assert(logMessage.contains("Bluetooth permissions were denied"))
    }
}
