package uk.gov.onelogin.sharing.bluetooth.advertiser

sealed interface AdvertiserState {
    data object Idle : AdvertiserState
    data object Starting : AdvertiserState
    data object Started : AdvertiserState
    data class Failed(val error: String) : AdvertiserState
    data object Stopping : AdvertiserState
    data object Stopped : AdvertiserState
}
