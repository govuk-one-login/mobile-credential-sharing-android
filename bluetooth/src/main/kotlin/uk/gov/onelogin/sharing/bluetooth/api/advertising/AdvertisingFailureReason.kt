package uk.gov.onelogin.sharing.bluetooth.api.advertising

import android.bluetooth.le.AdvertisingSetCallback

/**
 * Represents the possible reasons for a Bluetooth advertising failure.
 *
 * This class provides a platform-agnostic way to represent the error codes from Android's
 * [AdvertisingSetCallback].
 */
enum class AdvertisingFailureReason {
    /** The advertiser is not available. */
    ADVERTISER_NULL,

    /** The advertising operation failed due to a security exception. */
    ADVERTISE_FAILED_SECURITY_EXCEPTION,

    /** The advertising operation failed because it was already started. */
    ALREADY_STARTED,

    /** The advertising data is too large to be advertised. */
    ADVERTISE_FAILED_DATA_TOO_LARGE,

    /** An internal error occurred during the advertising operation. */
    ADVERTISE_FAILED_INTERNAL_ERROR,

    /** There are too many advertisers already active. */
    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,

    /** The requested advertising feature is not supported on this device. */
    ADVERTISE_FAILED_FEATURE_UNSUPPORTED,

    /** An unknown error occurred. */
    UNKNOWN;

    override fun toString(): String = when (this) {
        ADVERTISER_NULL -> "Advertiser is null"
        ADVERTISE_FAILED_SECURITY_EXCEPTION -> "Advertising failed due to security exception"
        ALREADY_STARTED -> "Advertising already started"
        ADVERTISE_FAILED_DATA_TOO_LARGE -> "Advertising failed due to data too large"
        ADVERTISE_FAILED_INTERNAL_ERROR -> "Advertising failed due to internal error"
        ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Advertising failed due to too many advertisers"
        ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Advertising failed due to feature unsupported"
        UNKNOWN -> "Unknown reason"
    }
}

/**
 * Converts an integer error code from [AdvertisingSetCallback] to an [AdvertisingFailureReason].
 *
 * @return The corresponding reason or if the code.
 */
fun Int.toReason(): AdvertisingFailureReason = when (this) {
    AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED
    -> AdvertisingFailureReason.ALREADY_STARTED

    AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE
    -> AdvertisingFailureReason.ADVERTISE_FAILED_DATA_TOO_LARGE

    AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED
    -> AdvertisingFailureReason.ADVERTISE_FAILED_FEATURE_UNSUPPORTED

    AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR
    -> AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR

    AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
    -> AdvertisingFailureReason.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS

    else -> AdvertisingFailureReason.UNKNOWN
}
