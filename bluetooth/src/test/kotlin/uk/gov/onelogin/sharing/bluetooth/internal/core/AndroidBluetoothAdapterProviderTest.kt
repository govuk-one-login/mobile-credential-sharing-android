package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.api.adapter.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.ble.BluetoothContext

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothAdapterProviderTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val mockAdapter: BluetoothAdapter = mockk()
    private val mockManager: BluetoothManager = mockk()
    private val context = BluetoothContext(app, mockManager)
    private val provider = AndroidBluetoothAdapterProvider(context)
    val mockAdvertiser: BluetoothLeAdvertiser = mockk()
    val mockLeScanner: BluetoothLeScanner = mockk()

    @Test
    fun `returns true when adapter is enabled`() {
        every { mockAdapter.isEnabled } returns true
        every { mockManager.adapter } returns mockAdapter

        Assert.assertTrue(provider.isEnabled())
    }

    @Test
    fun `returns false when adapter is disabled`() {
        every { mockAdapter.isEnabled } returns false
        every { mockManager.adapter } returns mockAdapter

        Assert.assertFalse(provider.isEnabled())
    }

    @Test
    fun `returns advertiser from adapter`() {
        every { mockAdapter.bluetoothLeAdvertiser } returns mockAdvertiser
        every { mockManager.adapter } returns mockAdapter

        Assert.assertEquals(mockAdvertiser, provider.getAdvertiser())
    }

    @Test
    fun `returns LE scanner from adapter`() {
        every { mockAdapter.bluetoothLeScanner } returns mockLeScanner
        every { mockManager.adapter } returns mockAdapter

        assertEquals(mockLeScanner, provider.getLeScanner())
    }
}
