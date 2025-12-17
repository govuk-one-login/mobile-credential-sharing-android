package uk.gov.onelogin.sharing.bluetooth.api.scanner

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
    fun `scan callback's onResult sends item to flow`() = runTest {
        val uuid = UUID.randomUUID().toBytes()
        val mockScanResult = mockk<ScanResult>(relaxed = true)
        val callbackSlot = slot<ScanCallback>()

        every {
            mockBluetoothLeScanner.startScan(any<List<ScanFilter>>(), any(), capture(callbackSlot))
        } returns Unit

        scanner.scan(uuid).test {
            callbackSlot.captured.onScanResult(0, mockScanResult)
            assertEquals(mockScanResult, awaitItem())
        }
    }

    @Test
    fun `scan callback's onFailure returns ScannerFailure message`() = runTest {
        val uuid = UUID.randomUUID().toBytes()
        val callbackSlot = slot<ScanCallback>()
        val expectedFailureCode = 1
        val expectedMessage = ScannerFailure.ALREADY_STARTED_SCANNING

        every {
            mockBluetoothLeScanner.startScan(any<List<ScanFilter>>(), any(), capture(callbackSlot))
        } returns Unit

        scanner.scan(uuid).test {
            callbackSlot.captured.onScanFailed(expectedFailureCode)

            val error = awaitError()
            assertEquals("Scan failed: $expectedMessage", error.message)
        }
    }
}
