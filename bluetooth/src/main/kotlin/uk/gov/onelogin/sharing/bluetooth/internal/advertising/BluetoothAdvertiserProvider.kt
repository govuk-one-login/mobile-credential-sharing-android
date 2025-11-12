package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData

interface BluetoothAdvertiserProvider {
    fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    fun stopAdvertisingSet()
}
