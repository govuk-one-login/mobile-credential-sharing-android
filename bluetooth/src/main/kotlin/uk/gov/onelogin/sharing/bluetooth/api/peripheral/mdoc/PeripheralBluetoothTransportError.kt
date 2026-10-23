package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothTransportErrorMessages
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerError

enum class PeripheralBluetoothTransportError(val message: String) {
    BLUETOOTH_TURNED_OFF(BluetoothTransportErrorMessages.BLUETOOTH_TURNED_OFF),
    ADVERTISING_FAILED("Advertising failed"),
    GATT_NOT_AVAILABLE(BluetoothTransportErrorMessages.GATT_NOT_AVAILABLE),
    BLUETOOTH_PERMISSION_MISSING(BluetoothTransportErrorMessages.BLUETOOTH_PERMISSION_MISSING),
    DESCRIPTOR_WRITE_REQUEST_FAILED("Descriptor write request failed"),
    SERVICE_REGISTRATION_FAILED("GATT service registration failed"),
    EXCEEDED_MAX_BUFFER_SIZE(BluetoothTransportErrorMessages.EXCEEDED_MAX_BUFFER_SIZE);

    companion object {
        fun fromGattError(gattServerError: GattServerError): PeripheralBluetoothTransportError =
            when (gattServerError) {
                GattServerError.ADVERTISING_FAILED -> ADVERTISING_FAILED
                GattServerError.GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                GattServerError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
                GattServerError.DESCRIPTOR_WRITE_REQUEST_FAILED -> DESCRIPTOR_WRITE_REQUEST_FAILED
                GattServerError.SERVICE_REGISTRATION_FAILED -> SERVICE_REGISTRATION_FAILED
                GattServerError.EXCEEDED_MAX_BUFFER_SIZE -> EXCEEDED_MAX_BUFFER_SIZE
            }
    }
}
