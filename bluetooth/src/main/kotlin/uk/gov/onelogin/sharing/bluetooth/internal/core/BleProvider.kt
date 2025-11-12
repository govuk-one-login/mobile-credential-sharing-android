package uk.gov.onelogin.sharing.bluetooth.internal.core

import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback

interface BleProvider {
    fun isBluetoothEnabled(): Boolean
    fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    fun stopAdvertising()
}
