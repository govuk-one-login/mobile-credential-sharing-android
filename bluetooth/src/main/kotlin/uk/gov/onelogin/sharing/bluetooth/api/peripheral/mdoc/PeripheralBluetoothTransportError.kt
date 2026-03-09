package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError

enum class PeripheralBluetoothTransportError {
    ADVERTISING_FAILED,

    GATT_NOT_AVAILABLE,

    BLUETOOTH_PERMISSION_MISSING,

    DESCRIPTOR_WRITE_REQUEST_FAILED;

    companion object {
        fun fromGattError(gattServerError: GattServerError): PeripheralBluetoothTransportError =
            when (gattServerError) {
                GattServerError.ADVERTISING_FAILED -> ADVERTISING_FAILED
                GattServerError.GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                GattServerError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
                GattServerError.DESCRIPTOR_WRITE_REQUEST_FAILED -> DESCRIPTOR_WRITE_REQUEST_FAILED
            }
    }
}
