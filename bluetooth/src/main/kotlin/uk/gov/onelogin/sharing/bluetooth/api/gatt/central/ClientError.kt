package uk.gov.onelogin.sharing.bluetooth.api.gatt.central

/**
 * Represents the possible errors that can occur during a BLE client operation.
 *
 * These errors cover issues related to service discovery, permissions, and GATT availability.
 */
enum class ClientError {
    /**
     * Indicates service discovered but status is not BluetoothGatt.GATT_SUCCESS
     */
    SERVICE_DISCOVERED_ERROR,

    /**
     * Indicates that the required service could not be found on the connected device.
     */
    SERVICE_NOT_FOUND,

    /**
     * Indicates that the necessary Bluetooth permissions are missing.
     */
    BLUETOOTH_PERMISSION_MISSING,

    /**
     * Indicates that the Bluetooth GATT instance is not available or could not be obtained.
     */
    BLUETOOTH_GATT_NOT_AVAILABLE
}
