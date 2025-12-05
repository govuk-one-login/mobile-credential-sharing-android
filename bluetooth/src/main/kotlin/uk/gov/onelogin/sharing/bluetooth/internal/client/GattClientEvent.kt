package uk.gov.onelogin.sharing.bluetooth.internal.client

import java.util.UUID

sealed interface GattClientEvent {
    data class Connected(val deviceAddress: String) : GattClientEvent
    data class Disconnected(val deviceAddress: String) : GattClientEvent
    data class ServicesDiscovered(val hasMdocService: Boolean) : GattClientEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattClientEvent
}