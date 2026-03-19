package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

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

    /**
     * The device has successfully connected.
     *
     * @param address The address of the connected device.
     */
    data class Connected(val address: String) : CentralBluetoothState

    /**
     * The device has disconnected.
     *
     * @param address The address of the disconnected device.
     * @param isSessionEnd Whether the disconnection was a deliberate session end.
     */
    data class Disconnected(val address: String, val isSessionEnd: Boolean) :
        CentralBluetoothState

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
}
