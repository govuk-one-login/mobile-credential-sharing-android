package uk.gov.onelogin.sharing.bluetooth.ble

class FakeBleProvider : BleProvider {
    var enabled = true
    var hasPermissions = true
    var thrownOnStart: Throwable? = null
    var parameters: AdvertisingParameters? = null
    var data: BleAdvertiseData? = null
    var callback: AdvertisingCallback? = null

    override fun isBluetoothEnabled(): Boolean = enabled

    override fun hasAdvertisePermission(): Boolean = hasPermissions

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        thrownOnStart?.let { throw it }
        this.parameters = parameters
        this.data = bleAdvertiseData
        this.callback = callback

        callback.onAdvertisingStarted()
    }

    override fun stopAdvertisingSet(callback: AdvertisingCallback?) {
        callback?.onAdvertisingStopped()
    }
}
