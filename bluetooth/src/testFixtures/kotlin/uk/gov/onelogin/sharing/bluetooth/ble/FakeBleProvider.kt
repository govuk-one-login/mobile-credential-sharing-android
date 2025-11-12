package uk.gov.onelogin.sharing.bluetooth.ble

class FakeBleProvider : BleProvider {
    var enabled = true
    var thrownOnStart: Throwable? = null
    var parameters: AdvertisingParameters? = null
    var data: BleAdvertiseData? = null
    var callback: AdvertisingCallback? = null

    override fun isBluetoothEnabled(): Boolean = enabled

    override fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        thrownOnStart?.let { throw it }
        this.parameters = parameters
        this.data = bleAdvertiseData
        this.callback = callback
    }

    override fun stopAdvertising() {
        callback?.onAdvertisingStopped()
    }

    fun triggerOnAdvertisingStarted() {
        callback?.onAdvertisingStarted()
    }

    fun triggerOnAdvertisingFailed(reason: AdvertisingFailureReason) {
        callback?.onAdvertisingStartFailed(reason)
    }

    fun triggerOnAdvertisingStopped() {
        callback?.onAdvertisingStopped()
    }
}
