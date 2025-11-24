package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice

sealed interface GattEvent {
    data class Error(val message: String) : GattEvent

    data class ConnectionStateChange(
        val status: Int,
        val newState: Int,
        val device: BluetoothDevice
    ) : GattEvent
}