package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.central.GattClientError

enum class CentralBluetoothTransportError {
    BLUETOOTH_TURNED_OFF,
    BLUETOOTH_PERMISSION_MISSING,
    GATT_NOT_AVAILABLE,
    SERVICE_NOT_FOUND,
    INVALID_SERVICE,
    FAILED_TO_SUBSCRIBE,
    FAILED_TO_START,
    SCAN_FAILED,
    INVALID_MESSAGE_PREFIX,
    SERVICE_CHANGED,
    EXCEEDED_MAX_BUFFER_SIZE;

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
