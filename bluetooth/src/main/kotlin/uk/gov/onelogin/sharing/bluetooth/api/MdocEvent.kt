package uk.gov.onelogin.sharing.bluetooth.api

sealed interface MdocEvent {
    data class Connected(val address: String) : MdocEvent
    data class Disconnected(val address: String) : MdocEvent
    data class Error(val error: MdocError) : MdocEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(
        val address: String,
        val status: Int,
        val newState: Int
    ) : MdocEvent
}