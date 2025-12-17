package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBluetoothAdapter
import org.robolectric.shadows.ShadowBluetoothLeScanner
import org.robolectric.shadows.ShadowBluetoothManager
import org.robolectric.shadows.ShadowLog
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.core.UUIDExtensions.toBytes

@RunWith(AndroidJUnit4::class)
@Config(
    shadows = [
        ShadowBluetoothLeScanner::class,
        ShadowBluetoothAdapter::class,
        ShadowBluetoothManager::class,
        ShadowLog::class
    ]
)
class AndroidBluetoothScannerTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    private lateinit var mockBluetoothAdapterProvider: BluetoothAdapterProvider
    private lateinit var mockBluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanner: AndroidBluetoothScanner

    @Before
    fun setUp() {
        mockBluetoothLeScanner = mockk(relaxed = true)
        mockBluetoothAdapterProvider = mockk()

        every { mockBluetoothAdapterProvider.getLeScanner() } returns mockBluetoothLeScanner

        scanner = AndroidBluetoothScanner(mockBluetoothAdapterProvider)
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
}
