package uk.gov.onelogin.sharing.bluetooth.ble

interface AdvertisingCallback {
    fun onAdvertisingStarted()
    fun onAdvertisingFailed(status: Int)
    fun onAdvertisingStopped()
}
