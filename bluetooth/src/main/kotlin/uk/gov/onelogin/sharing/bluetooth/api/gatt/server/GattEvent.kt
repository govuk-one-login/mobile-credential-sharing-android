package uk.gov.onelogin.sharing.bluetooth.api.gatt.server

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile

sealed interface GattEvent {
    data class ConnectionStateChange(
        val status: Int,
        val newState: Int,
        val device: BluetoothDevice
    ) : GattEvent {
        fun toGattServerEvent(): GattServerEvent {
            val address = device.address

            return when {
                status == BluetoothGatt.GATT_SUCCESS &&
                    newState == BluetoothProfile.STATE_CONNECTED ->
                    GattServerEvent.Connected(address)

                newState == BluetoothProfile.STATE_DISCONNECTED ->
                    GattServerEvent.Disconnected(address)

                else -> GattServerEvent.UnsupportedEvent(
                    device.address,
                    status,
                    newState
                )
            }
        }
    }

    data class ServiceAdded(val status: Int, val service: BluetoothGattService?) : GattEvent

    data object ConnectionStateStarted : GattEvent
}
