package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.le.ScanResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.BluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class SessionEstablishmentViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = true)
    val scanner = FakeAndroidBluetoothScanner()
    val logger = SystemLogger()

    val viewModel = SessionEstablishmentViewModel(
        bluetoothAdapterProvider = bluetoothAdapterProvider,
        scanner = scanner,
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = logger
    )

    @Test
    fun `init sets isBluetoothEnabled from adapter provider`() {
        bluetoothAdapterProvider.setEnabled(false)

        assertEquals(true, viewModel.uiState.value.isBluetoothEnabled)
    }

    @Test
    fun `scanForDevice calls scanner with provided uuid`() = runTest {
        val uuid = byteArrayOf(0x01, 0x02, 0x03)
        viewModel.scanForDevice(uuid)

        assertEquals(1, scanner.scanCalls)
        assertArrayEquals(uuid, scanner.lastUuid)
    }

//    @Test `stop scanning

    @Test
    fun `scanForDevice times out when no results emitted`() = runTest {
        val scanner = object : BluetoothScanner {
            override fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult> =
                callbackFlow {
                    awaitCancellation()
                }
        }

        val viewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = bluetoothAdapterProvider,
            scanner = scanner,
            dispatcher = mainDispatcherRule.testDispatcher,
            logger = logger
        )

        viewModel.scanForDevice(byteArrayOf(0x01, 0x02, 0x03))

        runCurrent()

        advanceTimeBy(SessionEstablishmentViewModel.SCAN_PERIOD)
        advanceUntilIdle()

        val logMessage = logger[0].message
        assert(logMessage.contains("TimeoutCancellationException:"))
    }

    @Test
    fun `stopScanning logs and cancels an active scan job`() = runTest {
        var flowClosed = false

        val scanner = object : BluetoothScanner {
            override fun scan(peripheralServerModeUuid: ByteArray): Flow<ScanResult> =
                callbackFlow {
                    awaitClose { flowClosed = true }
                }
        }

        val viewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = bluetoothAdapterProvider,
            scanner = scanner,
            dispatcher = mainDispatcherRule.testDispatcher,
            logger = logger
        )

        viewModel.scanForDevice(byteArrayOf(0x01))
        runCurrent()

        viewModel.stopScanning()
        runCurrent()

        assertTrue(
            "Expected scan flow to be closed after cancel",
            flowClosed
        )
    }
}
