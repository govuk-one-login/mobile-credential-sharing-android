package uk.gov.onelogin.sharing.bluetooth.ble

interface AdvertisingCallback {
    fun onAdvertisingStarted()
    fun onAdvertisingStartFailed(reason: AdvertisingFailureReason)
    fun onAdvertisingStopped()
}
