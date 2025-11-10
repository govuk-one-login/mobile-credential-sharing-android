package uk.gov.onelogin.sharing.bluetooth.ble

interface AdvertisingCallback {
    fun onAdvertisingStarted()
    fun onAdvertisingFailed(status: Status)
    fun onAdvertisingStopped()
}

sealed class Status {
    data object AlreadyStarted : Status()
    data class Error(val statusId: Int) : Status()
}
