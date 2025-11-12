package uk.gov.onelogin.sharing.bluetooth.ble

class AdvertisingCallbackStub : AdvertisingCallback {
    var started = false
    var stopped = false
    var reason: Reason? = null

    override fun onAdvertisingStarted() {
        started = true
    }

    override fun onAdvertisingStopped() {
        stopped = true
    }

    override fun onAdvertisingStartFailed(reason: Reason) {
        this.reason = reason
    }
}
