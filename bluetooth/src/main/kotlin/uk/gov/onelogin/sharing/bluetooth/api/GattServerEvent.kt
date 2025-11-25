package uk.gov.onelogin.sharing.bluetooth.api

sealed interface GattServerEvent {
    data class Connected(val address: String) : GattServerEvent
    data class Disconnected(val address: String?) : GattServerEvent
    data class Error(val error: MdocError) : GattServerEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattServerEvent
}
