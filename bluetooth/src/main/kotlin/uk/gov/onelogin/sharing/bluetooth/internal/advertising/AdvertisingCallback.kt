package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingFailureReason

interface AdvertisingCallback {
    fun onAdvertisingStarted()
    fun onAdvertisingStartFailed(reason: AdvertisingFailureReason)
    fun onAdvertisingStopped()
}
