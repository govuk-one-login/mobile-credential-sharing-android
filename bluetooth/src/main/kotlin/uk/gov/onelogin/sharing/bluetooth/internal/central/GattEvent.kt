package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent

internal interface GattEvent {
    data class ConnectionStateChange(val gatt: BluetoothGatt, val status: Int, val newState: Int) :
        GattEvent {
        fun toGattClientEvent(): GattClientEvent {
            val address = gatt.device.address

            return when {
                status == BluetoothGatt.GATT_SUCCESS &&
                    newState == BluetoothGatt.STATE_CONNECTED ->
                    GattClientEvent.Connected(address)

                newState == BluetoothGatt.STATE_DISCONNECTED ->
                    GattClientEvent.Disconnected(address)

                else -> GattClientEvent.UnsupportedEvent(
                    address,
                    status,
                    newState
                )
            }
        }
    }

    data class ServicesDiscovered(val gatt: BluetoothGatt, val status: Int) : GattEvent
}
