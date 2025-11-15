package uk.gov.onelogin.sharing.bluetooth.internal.core

import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BluetoothAdvertiserProvider

class AndroidBleProvider(
    private val bluetoothAdapter: BluetoothAdapterProvider,
    private val bleAdvertiser: BluetoothAdvertiserProvider?
) : BleProvider {

    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled()

    override fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        check(bleAdvertiser != null) {
            "Bluetooth advertiser not available"
        }

        bleAdvertiser.startAdvertisingSet(
            parameters,
            bleAdvertiseData,
            callback
        )
    }

    override fun stopAdvertising() {
        bleAdvertiser?.stopAdvertisingSet()
    }
}
