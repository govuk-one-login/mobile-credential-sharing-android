package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallbackEvent
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

/**
 * Represents the combined states from scanning and the GATT client.
 */
sealed interface CentralBluetoothState {
    /** This is the initial state. */
    data object Idle : CentralBluetoothState

    /** BLE scanning is in progress. */
    data object Scanning : CentralBluetoothState

    /** The device is connecting to a peripheral. */
    data object Connecting : CentralBluetoothState

    sealed interface HasDeviceAddress : CentralBluetoothState {
        val address: String
    }

    /**
     * The device has successfully connected.
     *
     * @param address The address of the connected device.
     */
    data class Connected(override val address: String) : HasDeviceAddress

    /**
     * The device has disconnected.
     *
     * @param address The address of the disconnected device.
     * @param isSessionEnd Whether the disconnection was a deliberate session end.
     */
    data class Disconnected(override val address: String, val isSessionEnd: Boolean) :
        HasDeviceAddress

    /**
     * The GATT connection state has been started (MTU negotiated, state characteristic written).
     */
    data object ConnectionStateStarted : CentralBluetoothState

    /**
     * An error occurred during the session.
     *
     * @param reason The [CentralBluetoothTransportError] that occurred.
     */
    data class Error(val reason: CentralBluetoothTransportError) : CentralBluetoothState

    /**
     * A session end command has been received.
     */
    data class CentralBluetoothEnded(val status: SessionEndStates) : CentralBluetoothState

    /**
     * A data structure has been received from the other device.
     *
     * @param uuid The bluetooth characteristic [UUID] where the message originates from.
     * @param value The completed data from the associated [uuid] characteristic.
     */
    data class Message(val uuid: UUID, val value: ByteArray) : CentralBluetoothState {

        constructor(
            message: GattClientEvent.Message
        ) : this(
            uuid = message.uuid,
            value = message.value
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Message

            if (uuid != other.uuid) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = uuid.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }
}
