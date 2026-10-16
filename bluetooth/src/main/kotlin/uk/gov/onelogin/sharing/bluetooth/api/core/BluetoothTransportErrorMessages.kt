package uk.gov.onelogin.sharing.bluetooth.api.core

/**
 * Shared error message strings used by both the central (GATT client) and peripheral
 * (GATT server) Bluetooth transport error enums.
 */
internal object BluetoothTransportErrorMessages {
    const val BLUETOOTH_TURNED_OFF = "Bluetooth is currently turned off"
    const val BLUETOOTH_PERMISSION_MISSING = "Bluetooth permission missing"
    const val GATT_NOT_AVAILABLE = "GATT not available"
    const val EXCEEDED_MAX_BUFFER_SIZE = "Received data exceeded maximum buffer size"
}
