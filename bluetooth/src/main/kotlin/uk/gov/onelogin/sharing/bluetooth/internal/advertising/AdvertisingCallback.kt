package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertisingFailureReason

/**
 * Abstracts the underlying Android `AdvertisingSetCallback` and communicates
 * the status of advertising operations back to the [AndroidBleAdvertiser].
 */
interface AdvertisingCallback {
    /**
     * Advertising operation has started successfully.
     */
    fun onAdvertisingStarted()

    /**
     * Advertising operation fails to start.
     *
     * @param reason The [AdvertisingFailureReason] indicates why the operation failed.
     */
    fun onAdvertisingStartFailed(reason: AdvertisingFailureReason)

    /**
     * Advertising operation has stopped.
     */
    fun onAdvertisingStopped()
}
