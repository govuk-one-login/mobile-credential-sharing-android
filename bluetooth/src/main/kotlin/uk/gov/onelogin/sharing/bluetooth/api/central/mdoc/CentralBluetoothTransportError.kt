package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError

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
        fun fromClientError(clientError: ClientError): CentralBluetoothTransportError =
            when (clientError) {
                ClientError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
                ClientError.BLUETOOTH_GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                ClientError.SERVICE_NOT_FOUND -> SERVICE_NOT_FOUND
                ClientError.INVALID_SERVICE -> INVALID_SERVICE
                ClientError.FAILED_TO_SUBSCRIBE -> FAILED_TO_SUBSCRIBE
                ClientError.FAILED_TO_START -> FAILED_TO_START
                ClientError.SERVICE_DISCOVERED_ERROR -> INVALID_SERVICE
                ClientError.INVALID_MESSAGE_PREFIX -> INVALID_MESSAGE_PREFIX
                ClientError.SERVICE_CHANGED -> SERVICE_CHANGED
                ClientError.EXCEEDED_MAX_BUFFER_SIZE -> EXCEEDED_MAX_BUFFER_SIZE
            }
    }
}
