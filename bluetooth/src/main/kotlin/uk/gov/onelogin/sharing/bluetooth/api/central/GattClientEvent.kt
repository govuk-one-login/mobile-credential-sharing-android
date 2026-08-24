package uk.gov.onelogin.sharing.bluetooth.api.central

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

sealed interface GattClientEvent {
    data object Connecting : GattClientEvent
    data class Connected(val deviceAddress: String) : GattClientEvent
    data class Disconnected(val deviceAddress: String, val isSessionEnd: Boolean) : GattClientEvent

    data class Error(val error: ClientClientError) : GattClientEvent
    data object ConnectionStateStarted : GattClientEvent

    // use for any functionality that has not been implemented yet
    data class UnsupportedEvent(val address: String, val status: Int, val newState: Int) :
        GattClientEvent

    data class SessionEnd(val sessionEndStates: SessionEndStates) : GattClientEvent

    data class Message(val uuid: UUID, val value: ByteArray) : GattClientEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Message

            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int = value.contentHashCode()
    }
}
