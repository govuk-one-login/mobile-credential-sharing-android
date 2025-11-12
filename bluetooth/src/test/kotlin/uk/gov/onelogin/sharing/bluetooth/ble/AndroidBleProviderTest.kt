package uk.gov.onelogin.sharing.bluetooth.ble

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.FakeBluetoothAdapterProvider

class AndroidBleProviderTest {
    val fakeAdapter = FakeBluetoothAdapterProvider(true)
    val fakeAdvertiser = FakeBluetoothAdvertiserProvider()
    val provider = AndroidBleProvider(
        fakeAdapter,
        fakeAdvertiser
    )

    @Test
    fun `bluetooth enabled returns true when bluetooth is enabled`() {
        assert(provider.isBluetoothEnabled())
    }

    @Test
    fun `bluetooth enabled returns false when bluetooth is disabled`() {
        fakeAdapter.setEnabled(false)

        assert(!provider.isBluetoothEnabled())
    }

    @Test
    fun `start advertising throws when advertiser provider is null`() {
        val provider = AndroidBleProvider(
            fakeAdapter,
            bleAdvertiser = null
        )

        val exception = assertThrows(IllegalStateException::class.java) {
            provider.startAdvertising(
                AdvertisingParameters(),
                stubBleAdvertiseData(),
                object : AdvertisingCallback {
                    override fun onAdvertisingStarted() {
                        println("advertising started")
                    }

                    override fun onAdvertisingStopped() {
                        println("advertising stopped")
                    }

                    override fun onAdvertisingStartFailed(reason: AdvertisingFailureReason) {
                        println("advertising failed reason")
                    }
                }
            )
        }
        assertEquals("Bluetooth advertiser not available", exception.message)
    }

    @Test
    fun `start advertising delegates to advertiser with same params and callback`() {
        val parameters = AdvertisingParameters()
        val bleAdvertiseData = stubBleAdvertiseData()
        val callback = object : AdvertisingCallback {
            override fun onAdvertisingStarted() {
                println("advertising started")
            }

            override fun onAdvertisingStopped() {
                println("advertising stopped")
            }

            override fun onAdvertisingStartFailed(reason: AdvertisingFailureReason) {
                println("advertising failed reason")
            }
        }

        provider.startAdvertising(
            parameters,
            bleAdvertiseData,
            callback
        )

        assert(fakeAdvertiser.startCalled == 1)
        assert(fakeAdvertiser.parameters == parameters)
        assert(fakeAdvertiser.bleAdvertiseData == bleAdvertiseData)
        assert(fakeAdvertiser.callback == callback)
    }

    @Test
    fun `stop advertising delegates to advertiser`() {
        provider.stopAdvertising()

        assert(fakeAdvertiser.stopCalled == 1)
    }

    @Test
    fun `stop advertising does nothing when advertiser provider is null`() {
        val provider = AndroidBleProvider(fakeAdapter, bleAdvertiser = null)

        provider.stopAdvertising()

        assert(fakeAdvertiser.stopCalled == 0)
    }
}
