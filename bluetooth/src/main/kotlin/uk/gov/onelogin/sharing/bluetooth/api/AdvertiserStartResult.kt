package uk.gov.onelogin.sharing.bluetooth.api

/**
 * Represents the result of a Bluetooth Low Energy advertiser start operation.
 *
 */
sealed interface AdvertiserStartResult {
    /** Operation started successfully. */
    data object Success : AdvertiserStartResult

    /**
     * Advertising operation failed to start.
     *
     * @param error A string describing the reason for the failure.
     */
    data class Error(val error: String) : AdvertiserStartResult
}
