package uk.gov.onelogin.sharing.bluetooth.internal.core

import org.junit.Assert
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallbackStub
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingParameters

internal class AndroidBleProviderTest {
    val fakeAdapter = FakeBluetoothAdapterProvider(true)
    val fakeAdvertiser = FakeBluetoothAdvertiserProvider()
    val provider: BleProvider = AndroidBleProvider(
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

        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            provider.startAdvertising(
                AdvertisingParameters(),
                stubBleAdvertiseData(),
                AdvertisingCallbackStub()
            )
        }
        Assert.assertEquals("Bluetooth advertiser not available", exception.message)
    }

    @Test
    fun `start advertising delegates to advertiser with same params and callback`() {
        val parameters = AdvertisingParameters()
        val bleAdvertiseData = stubBleAdvertiseData()
        val callback = AdvertisingCallbackStub()

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
