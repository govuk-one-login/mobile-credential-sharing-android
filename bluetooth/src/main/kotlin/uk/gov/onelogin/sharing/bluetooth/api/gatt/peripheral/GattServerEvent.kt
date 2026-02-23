package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import android.bluetooth.BluetoothGattService
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

sealed interface GattServerEvent {
    data class Connected(val address: String) : GattServerEvent
    data class Disconnected(val address: String?, val isSessionEnd: Boolean) : GattServerEvent
    data class ServiceAdded(val status: Int, val service: BluetoothGattService?) : GattServerEvent
    data object SessionStarted : GattServerEvent
    data object ServiceStopped : GattServerEvent
    data class Error(val error: GattServerError) : GattServerEvent
    data class SessionEnd(val status: SessionEndStates) : GattServerEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattServerEvent
}
