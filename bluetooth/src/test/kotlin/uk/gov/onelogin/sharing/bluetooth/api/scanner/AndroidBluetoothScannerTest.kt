package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.core.UUIDExtensions.toBytes

@RunWith(AndroidJUnit4::class)
class AndroidBluetoothScannerTest {
    private lateinit var mockBluetoothAdapterProvider: BluetoothAdapterProvider
    private lateinit var mockBluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanner: AndroidBluetoothScanner
    private val logger = SystemLogger()

    @Before
    fun setUp() {
        mockBluetoothLeScanner = mockk(relaxed = true)
        mockBluetoothAdapterProvider = mockk()

        every { mockBluetoothAdapterProvider.getLeScanner() } returns mockBluetoothLeScanner

        scanner = AndroidBluetoothScanner(mockBluetoothAdapterProvider, logger)
    }

    @Test
    fun `scan emits DeviceFound event`() = runTest {
        val uuid = UUID.randomUUID().toBytes()

        val mockDevice = mockk<BluetoothDevice>()
        every { mockDevice.address } returns "AA:BB:CC:DD:EE:FF"

        val mockScanResult = mockk<ScanResult>()
        every { mockScanResult.device } returns mockDevice

        val callbackSlot = slot<ScanCallback>()
        every {
            mockBluetoothLeScanner.startScan(any<List<ScanFilter>>(), any(), capture(callbackSlot))
        } returns Unit

        val flow = scanner.scan(uuid)

        flow.test {
            callbackSlot.captured.onScanResult(0, mockScanResult)

            val emitted = awaitItem()

            assertTrue(emitted is ScanEvent.DeviceFound)
            assertEquals("AA:BB:CC:DD:EE:FF", (emitted as ScanEvent.DeviceFound).deviceAddress)
        }
    }

    @Test
    fun `scan fails with ScannerFailure Already_Started_Scanning when scan is in progress`() =
        runTest {
            val uuid = UUID.randomUUID().toBytes()
            val callbackSlot = slot<ScanCallback>()

            every {
                mockBluetoothLeScanner.startScan(
                    any<List<ScanFilter>>(),
                    any(),
                    capture(callbackSlot)
                )
            } returns Unit

            val firstScanJob = launch {
                scanner.scan(uuid).collect()
            }

            delay(100)

            val secondScanFlow = scanner.scan(uuid)

            secondScanFlow.test {
                val emitted = awaitItem()

                assertTrue(emitted is ScanEvent.ScanFailed)
                assertEquals(
                    ScannerFailure.ALREADY_STARTED_SCANNING,
                    (emitted as ScanEvent.ScanFailed).failure
                )
            }

            firstScanJob.cancel()
        }

    @Test
    fun `scan emits ScanFailed event`() = runTest {
        val uuid = UUID.randomUUID().toBytes()
        val callbackSlot = slot<ScanCallback>()

        every {
            mockBluetoothLeScanner.startScan(any<List<ScanFilter>>(), any(), capture(callbackSlot))
        } returns Unit

        val flow = scanner.scan(uuid)

        flow.test {
            callbackSlot.captured.onScanFailed(ScanCallback.SCAN_FAILED_ALREADY_STARTED)

            val emitted = awaitItem()
            val actualScannerFailure = (emitted as ScanEvent.ScanFailed).failure

            assertEquals(ScannerFailure.ALREADY_STARTED_SCANNING, actualScannerFailure)
        }
    }

    @Test
    fun `scan fails ScannerFailure Internal_Error if scanner is null`() = runTest {
        val uuid = UUID.randomUUID().toBytes()
        val mockAdapterProvider = mockk<BluetoothAdapterProvider>()
        every { mockAdapterProvider.getLeScanner() } returns null

        val scannerWithNullAdapter = AndroidBluetoothScanner(
            bluetoothAdapterProvider = mockAdapterProvider,
            logger = logger
        )

        val flow = scannerWithNullAdapter.scan(uuid)

        flow.test {
            val emitted = awaitItem()
            assertEquals(
                ScannerFailure.INTERNAL_ERROR,
                (emitted as ScanEvent.ScanFailed).failure
            )
        }
    }
}
