package uk.gov.onelogin.sharing.bluetooth.internal.client

internal sealed interface GattClientEvent {
    data object Connecting : GattClientEvent
    data class Connected(val deviceAddress: String) : GattClientEvent
    data class Disconnected(val deviceAddress: String) : GattClientEvent
    data class ServicesDiscovered(val hasMdocService: Boolean) : GattClientEvent
    data class Error(val error: ClientError) : GattClientEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattClientEvent
}