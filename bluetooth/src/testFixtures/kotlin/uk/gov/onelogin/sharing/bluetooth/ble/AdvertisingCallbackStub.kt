package uk.gov.onelogin.sharing.bluetooth.ble

class AdvertisingCallbackStub : AdvertisingCallback {
    var started = false
    var stopped = false
    var failed: Status? = null

    override fun onAdvertisingStarted() {
        started = true
    }

    override fun onAdvertisingStopped() {
        stopped = true
    }

    override fun onAdvertisingFailed(status: Status) {
        failed = status
    }
}
