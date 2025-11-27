package uk.gov.onelogin.sharing.bluetooth.ble

import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingFailureReason

class AdvertisingCallbackStub : AdvertisingCallback {
    var started = false
    var stopped = false
    var advertisingFailureReason: AdvertisingFailureReason? = null

    override fun onAdvertisingStarted() {
        started = true
    }

    override fun onAdvertisingStopped() {
        stopped = true
    }

    override fun onAdvertisingStartFailed(reason: AdvertisingFailureReason) {
        this.advertisingFailureReason = reason
    }
}
