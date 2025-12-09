package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.BluetoothGattService

sealed interface GattServerEvent {
    data class Connected(val address: String) : GattServerEvent
    data class Disconnected(val address: String?) : GattServerEvent
    data class ServiceAdded(val status: Int, val service: BluetoothGattService?) : GattServerEvent
    data object SessionStarted : GattServerEvent
    data object ServiceStopped : GattServerEvent
    data class Error(val error: MdocSessionError) : GattServerEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattServerEvent
}
