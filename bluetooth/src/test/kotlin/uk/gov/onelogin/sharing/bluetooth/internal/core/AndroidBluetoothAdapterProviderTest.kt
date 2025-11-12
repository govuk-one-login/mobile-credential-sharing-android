package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.internal.util.BluetoothContext

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothAdapterProviderTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val mockAdapter: BluetoothAdapter = Mockito.mock()
    private val mockManager: BluetoothManager = Mockito.mock()
    private val context = BluetoothContext(app, mockManager)
    private val provider = AndroidBluetoothAdapterProvider(context)
    val mockAdvertiser: BluetoothLeAdvertiser = Mockito.mock()

    @Test
    fun `returns true when adapter is enabled`() {
        Mockito.`when`(mockAdapter.isEnabled)
            .thenReturn(true)

        Mockito.`when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        Assert.assertTrue(provider.isEnabled())
    }

    @Test
    fun `returns false when adapter is disabled`() {
        Mockito.`when`(mockAdapter.isEnabled)
            .thenReturn(false)

        Mockito.`when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        Assert.assertFalse(provider.isEnabled())
    }

    @Test
    fun `returns advertiser from adapter`() {
        Mockito.`when`(mockAdapter.bluetoothLeAdvertiser)
            .thenReturn(mockAdvertiser)

        Mockito.`when`(mockManager.adapter)
            .thenReturn(mockAdapter)

        Assert.assertEquals(mockAdvertiser, provider.getAdvertiser())
    }
}
