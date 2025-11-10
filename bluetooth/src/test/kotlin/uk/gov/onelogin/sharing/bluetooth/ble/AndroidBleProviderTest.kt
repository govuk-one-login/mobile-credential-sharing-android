package uk.gov.onelogin.sharing.bluetooth.ble

import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.BluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.FakeBluetoothAdapterProvider

class AndroidBleProviderTest {

    @Test
    fun `bluetooth enabled returns true when bluetooth is enabled`() {
        val fakeBluetoothAdapter = FakeBluetoothAdapterProvider(true)
        val fakeBluetoothAdapterProvider = FakeBluetoothAdvertiserProvider()

        val bluetoothProvider = AndroidBleProvider(
            fakeBluetoothAdapter,
            fakeBluetoothAdapterProvider
        )

        assert(bluetoothProvider.isBluetoothEnabled())
    }

    @Test
    fun `bluetooth enabled returns false when bluetooth is disabled`() {
        val fakeBluetoothAdapter = FakeBluetoothAdapterProvider(false)
        val fakeBluetoothAdapterProvider = FakeBluetoothAdvertiserProvider()
        val bluetoothProvider = AndroidBleProvider(
            fakeBluetoothAdapter,
            fakeBluetoothAdapterProvider
        )

        assert(!bluetoothProvider.isBluetoothEnabled())
    }
}

class FakeBluetoothAdvertiserProvider : BluetoothAdvertiserProvider {
    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        println("Advertising started")
    }

    override fun stopAdvertisingSet() {
        println("Advertising stopped")
    }
}
