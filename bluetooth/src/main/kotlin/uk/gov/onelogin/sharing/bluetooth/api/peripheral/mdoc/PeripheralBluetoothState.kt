package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates

/**
 * Represents the combined states from advertising and the GATT service.
 */
sealed interface PeripheralBluetoothState {
    /** This is the initial state. */
    data object Idle : PeripheralBluetoothState

    sealed interface HasDeviceAddress : PeripheralBluetoothState {
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
     * @param address The address of the disconnected device, which may be null if the address
     * is not known.
     */
    data class Disconnected(override val address: String, val isSessionEnd: Boolean) :
        HasDeviceAddress

    /**
     * An error occurred during the session. This can be and error
     * from the Advertiser or the GATT service
     *
     * @param reason The [PeripheralBluetoothTransportError] that occurred.
     */
    data class Error(val reason: PeripheralBluetoothTransportError) : PeripheralBluetoothState

    /**
     * A session end command has been received from the client or server manager
     *
     */
    data class Ended(val status: SessionEndStates) : PeripheralBluetoothState

    data class MessageReceived(val message: ByteArray) : PeripheralBluetoothState
}
