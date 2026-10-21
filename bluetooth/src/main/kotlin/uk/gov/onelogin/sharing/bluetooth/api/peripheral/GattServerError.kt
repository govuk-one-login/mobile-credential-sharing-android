package uk.gov.onelogin.sharing.bluetooth.api.peripheral

/**
 * Represents the possible errors that can occur during a BLE server operation.
 *
 * These errors cover issues related to advertising, GATT availability,
 * service registration, and message handling.
 */
enum class GattServerError {
    /**
     * Indicates that BLE advertising failed to start.
     */
    ADVERTISING_FAILED,

    /**
     * Indicates that the Bluetooth GATT server is not available or could not be obtained.
     */
    GATT_NOT_AVAILABLE,

    /**
     * Indicates that the necessary Bluetooth permissions are missing.
     */
    BLUETOOTH_PERMISSION_MISSING,

    /**
     * Indicates that a descriptor write request from the connected device failed.
     */
    DESCRIPTOR_WRITE_REQUEST_FAILED,

    /**
     * Indicates that the GATT service could not be registered with the GATT server.
     */
    SERVICE_REGISTRATION_FAILED,

    /**
     * Indicates that the accumulated receive buffer exceeded the configured maximum size.
     */
    EXCEEDED_MAX_BUFFER_SIZE
}
