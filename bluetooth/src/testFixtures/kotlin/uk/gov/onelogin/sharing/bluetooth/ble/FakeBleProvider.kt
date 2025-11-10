package uk.gov.onelogin.sharing.bluetooth.ble

class FakeBleProvider : BleProvider {
    var enabled = true
    var thrownOnStart: Throwable? = null
    var parameters: AdvertisingParameters? = null
    var data: BleAdvertiseData? = null
    var callback: AdvertisingCallback? = null

    override fun isBluetoothEnabled(): Boolean = enabled

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        thrownOnStart?.let { throw it }
        this.parameters = parameters
        this.data = bleAdvertiseData
        this.callback = callback
    }

    override fun stopAdvertisingSet() {
        callback?.onAdvertisingStopped()
    }

    fun triggerOnAdvertisingStarted() {
        callback?.onAdvertisingStarted()
    }

    fun triggerOnAdvertisingFailed(status: Int) {
        callback?.onAdvertisingFailed(Status.Error(status))
    }

    fun triggerOnAdvertisingStopped() {
        callback?.onAdvertisingStopped()
    }
}
