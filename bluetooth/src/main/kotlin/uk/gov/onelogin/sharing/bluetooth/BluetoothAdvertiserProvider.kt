package uk.gov.onelogin.sharing.bluetooth

import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData

interface BluetoothAdvertiserProvider {
    fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    fun stopAdvertisingSet()
}
