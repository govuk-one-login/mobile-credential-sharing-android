package uk.gov.onelogin.sharing.bluetooth.api

data class StartAdvertisingException(val error: AdvertisingError) : Exception("Error: $error")

/**
 * Represents specific failure conditions that can occur when starting
 * Bluetooth LE advertising.
 *
 * Each value corresponds to a known category of failure that can be
 * safely communicated to callers via [StartAdvertisingException].
 */
enum class AdvertisingError {

    BLUETOOTH_DISABLED,

    MISSING_PERMISSION,

    ALREADY_IN_PROGRESS,

    INVALID_UUID,

    START_TIMEOUT,

    CANCELLED,

    INTERNAL_ERROR;

    override fun toString(): String = when (this) {
        BLUETOOTH_DISABLED -> "Bluetooth is disabled"
        MISSING_PERMISSION -> "Missing bluetooth permission"
        ALREADY_IN_PROGRESS -> "Advertising already in progress"
        INVALID_UUID -> "Invalid UUID"
        START_TIMEOUT -> "Start advertising timed out"
        CANCELLED -> "Start advertising cancelled"
        INTERNAL_ERROR -> "Internal error"
    }
}
