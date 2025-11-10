package uk.gov.onelogin.sharing.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothAdapterProviderTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val mockAdapter: BluetoothAdapter = mock()
    private val mockManager: BluetoothManager = mock()
    private val context = BluetoothContext(app, mockManager)
    private val provider = AndroidBluetoothAdapterProvider(context)
    val mockAdvertiser: BluetoothLeAdvertiser = mock()

    @Test
    fun `returns true when adapter is enabled`() {
        `when`(mockAdapter.isEnabled)
            .thenReturn(true)

        `when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        assertTrue(provider.isEnabled())
    }

    @Test
    fun `returns false when adapter is disabled`() {
        `when`(mockAdapter.isEnabled)
            .thenReturn(false)

        `when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        assertFalse(provider.isEnabled())
    }

    @Test
    fun `returns advertiser from adapter`() {
        `when`(mockAdapter.bluetoothLeAdvertiser)
            .thenReturn(mockAdvertiser)

        `when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        assertEquals(mockAdvertiser, provider.getAdvertiser())
    }
}
