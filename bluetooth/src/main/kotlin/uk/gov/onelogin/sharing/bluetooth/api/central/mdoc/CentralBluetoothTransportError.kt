package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientError

enum class CentralBluetoothTransportError(val message: String) {
    BLUETOOTH_TURNED_OFF("Bluetooth is currently turned off"),
    BLUETOOTH_PERMISSION_MISSING("Bluetooth permission missing"),
    GATT_NOT_AVAILABLE("GATT not available"),
    SERVICE_NOT_FOUND("Failed to find the expected GATT service"),
    INVALID_SERVICE("GATT service did not include the expected characteristics"),
    FAILED_TO_SUBSCRIBE("Failed to subscribe to characteristics for communication"),
    FAILED_TO_START("Failed to set the state characteristic to 'Start'"),
    SCAN_FAILED("Bluetooth scan failed"),
    INVALID_MESSAGE_PREFIX("Received message with an invalid prefix byte"),
    SERVICE_CHANGED("GATT services changed during an active session"),
    EXCEEDED_MAX_BUFFER_SIZE("Received data exceeded maximum buffer size");

    companion object {
        fun fromClientError(gattClientError: GattClientError): CentralBluetoothTransportError =
            when (gattClientError) {
                GattClientError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
                GattClientError.BLUETOOTH_GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                GattClientError.SERVICE_NOT_FOUND -> SERVICE_NOT_FOUND
                GattClientError.INVALID_SERVICE -> INVALID_SERVICE
                GattClientError.FAILED_TO_SUBSCRIBE -> FAILED_TO_SUBSCRIBE
                GattClientError.FAILED_TO_START -> FAILED_TO_START
                GattClientError.SERVICE_DISCOVERED_ERROR -> INVALID_SERVICE
                GattClientError.INVALID_MESSAGE_PREFIX -> INVALID_MESSAGE_PREFIX
                GattClientError.SERVICE_CHANGED -> SERVICE_CHANGED
                GattClientError.EXCEEDED_MAX_BUFFER_SIZE -> EXCEEDED_MAX_BUFFER_SIZE
            }
    }
}
