package uk.gov.onelogin.sharing.holder.mdoc

import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerError

enum class MdocSessionError {
    ADVERTISING_FAILED,

    GATT_NOT_AVAILABLE,

    BLUETOOTH_PERMISSION_MISSING;

    companion object {
        fun fromGattError(gattServerError: GattServerError): MdocSessionError =
            when (gattServerError) {
                GattServerError.ADVERTISING_FAILED -> ADVERTISING_FAILED
                GattServerError.GATT_NOT_AVAILABLE -> GATT_NOT_AVAILABLE
                GattServerError.BLUETOOTH_PERMISSION_MISSING -> BLUETOOTH_PERMISSION_MISSING
            }
    }
}
