package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.central.ClientClientError

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
        fun fromClientError(clientClientError: ClientClientError): CentralBluetoothTransportError =
            when (clientClientError) {
                ClientClientError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
                ClientClientError.BLUETOOTH_GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                ClientClientError.SERVICE_NOT_FOUND -> SERVICE_NOT_FOUND
                ClientClientError.INVALID_SERVICE -> INVALID_SERVICE
                ClientClientError.FAILED_TO_SUBSCRIBE -> FAILED_TO_SUBSCRIBE
                ClientClientError.FAILED_TO_START -> FAILED_TO_START
                ClientClientError.SERVICE_DISCOVERED_ERROR -> INVALID_SERVICE
                ClientClientError.INVALID_MESSAGE_PREFIX -> INVALID_MESSAGE_PREFIX
                ClientClientError.SERVICE_CHANGED -> SERVICE_CHANGED
                ClientClientError.EXCEEDED_MAX_BUFFER_SIZE -> EXCEEDED_MAX_BUFFER_SIZE
            }
    }
}
