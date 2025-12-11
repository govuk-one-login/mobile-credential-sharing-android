package uk.gov.onelogin.sharing.bluetooth.api.advertising

/**
 * Represents the various states of a Bluetooth Low Energy (BLE) advertiser.
 *
 * This is used to model the lifecycle of the advertiser.
 */
sealed interface AdvertiserState {
    /** The advertiser is idle and not performing any operations. This is the initial state. */
    data object Idle : AdvertiserState

    /** The advertiser is in the process of starting. */
    data object Starting : AdvertiserState

    /** The advertiser has successfully started. */
    data object Started : AdvertiserState

    /** The advertiser has failed to start. */
    data class Failed(val error: String) : AdvertiserState

    /** The advertiser is in the process of stopping. */
    data object Stopping : AdvertiserState

    /** The advertiser has stopped. */
    data object Stopped : AdvertiserState
}
