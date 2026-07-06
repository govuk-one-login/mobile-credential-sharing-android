package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender

/**
 * Responsible for orchestrating BLE scanning and GATT client connection.
 *
 * Exposes [CentralBluetoothState] via a [StateFlow].
 */
interface CentralBluetoothTransport :
    MessageSender,
    BluetoothTransport<CentralBluetoothState> {

    val isBleOpen: Boolean

    /**
     * Sends the GATT End (0x02) command to the holder without disconnecting.
     * Used as part of the ISO 18013-5 §8.3.3.1.3 session termination protocol.
     */
    suspend fun sendEnd()

    /**
     * Stops the BLE session, optionally sending a session end command first.
     */
    suspend fun stop()
}
