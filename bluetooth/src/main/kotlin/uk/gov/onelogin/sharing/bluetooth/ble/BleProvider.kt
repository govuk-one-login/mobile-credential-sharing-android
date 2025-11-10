package uk.gov.onelogin.sharing.bluetooth.ble

interface BleProvider {
    fun isBluetoothEnabled(): Boolean
    fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    fun stopAdvertisingSet()
}
