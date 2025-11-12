package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.le.AdvertisingSetCallback

enum class AdvertisingFailureReason {
    ADVERTISER_NULL,

    ADVERTISE_FAILED_SECURITY_EXCEPTION,

    ALREADY_STARTED,

    ADVERTISE_FAILED_DATA_TOO_LARGE,

    ADVERTISE_FAILED_INTERNAL_ERROR,

    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS,

    ADVERTISE_FAILED_FEATURE_UNSUPPORTED,

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
