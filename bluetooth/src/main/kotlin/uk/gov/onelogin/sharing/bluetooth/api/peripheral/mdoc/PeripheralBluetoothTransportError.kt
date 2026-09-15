package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerError

enum class PeripheralBluetoothTransportError(val message: String) {
    BLUETOOTH_TURNED_OFF("Bluetooth is currently turned off"),
    ADVERTISING_FAILED("Advertising failed"),
    GATT_NOT_AVAILABLE("GATT not available"),
    BLUETOOTH_PERMISSION_MISSING("Bluetooth permission missing"),
    DESCRIPTOR_WRITE_REQUEST_FAILED("Descriptor write request failed"),
    SERVICE_REGISTRATION_FAILED("GATT service registration failed"),
    EXCEEDED_MAX_BUFFER_SIZE("Received data exceeded maximum buffer size");

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
