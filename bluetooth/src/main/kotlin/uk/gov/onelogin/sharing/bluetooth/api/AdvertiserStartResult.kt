package uk.gov.onelogin.sharing.bluetooth.api

sealed interface AdvertiserStartResult {
    data object Success : AdvertiserStartResult
    data class Error(val error: String) : AdvertiserStartResult
}
