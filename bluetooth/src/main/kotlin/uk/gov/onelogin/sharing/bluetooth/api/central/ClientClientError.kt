package uk.gov.onelogin.sharing.bluetooth.api.central

/**
 * Represents the possible errors that can occur during a BLE client operation.
 *
 * These errors cover issues related to service discovery, permissions, and GATT availability.
 */
enum class ClientClientError {
    /**
     * Indicates service discovered but status is not BluetoothGatt.GATT_SUCCESS
     */
    SERVICE_DISCOVERED_ERROR,

    /**
     * Indicates that the required service could not be found on the connected device.
     */
    SERVICE_NOT_FOUND,

    /**
     * Indicates that the service did not include the expected characteristics
     */
    INVALID_SERVICE,

    /**
     * Indicates that the necessary Bluetooth permissions are missing.
     */
    BLUETOOTH_PERMISSION_MISSING,

    /**
     * Indicates that the Bluetooth GATT instance is not available or could not be obtained.
     */
    BLUETOOTH_GATT_NOT_AVAILABLE,

    /**
     * Indicates that the Bluetooth GATT client could not subscribe to characteristics for communication
     */
    FAILED_TO_SUBSCRIBE,

    /**
     * Indicates that the Bluetooth GATT client failed to set the state characteristic to 'Start'
     */
    FAILED_TO_START,

    /**
     * Indicates that a Bluetooth characteristic value's first byte is invalid.
     */
    INVALID_MESSAGE_PREFIX,

    /**
     * Indicates that the remote GATT server's services changed during an active session,
     * meaning the current session is no longer valid.
     */
    SERVICE_CHANGED,

    /**
     * Indicates that the accumulated receive buffer exceeded the configured maximum size.
     */
    EXCEEDED_MAX_BUFFER_SIZE
}
