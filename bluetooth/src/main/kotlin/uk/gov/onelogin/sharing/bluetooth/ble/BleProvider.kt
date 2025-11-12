package uk.gov.onelogin.sharing.bluetooth.ble

interface BleProvider {
    fun isBluetoothEnabled(): Boolean
    fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    fun stopAdvertising()
}
