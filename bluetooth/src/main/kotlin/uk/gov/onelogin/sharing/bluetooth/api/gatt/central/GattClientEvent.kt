package uk.gov.onelogin.sharing.bluetooth.api.gatt.central

import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

sealed interface GattClientEvent {
    data object Connecting : GattClientEvent
    data class Connected(val deviceAddress: String) : GattClientEvent
    data class Disconnected(val deviceAddress: String, val isSessionEnd: Boolean) : GattClientEvent

    data class Error(val error: ClientError) : GattClientEvent
    data object ConnectionStateStarted : GattClientEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattClientEvent

    data class SessionEnd(val sessionEndStates: SessionEndStates) : GattClientEvent
}
