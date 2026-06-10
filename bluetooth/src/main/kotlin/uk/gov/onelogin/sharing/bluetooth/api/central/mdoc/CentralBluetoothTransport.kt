package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.core.MessageSender

/**
 * Responsible for orchestrating BLE scanning and GATT client connection.
 *
 * Exposes [CentralBluetoothState] via a [StateFlow].
 */
interface CentralBluetoothTransport : MessageSender, BluetoothTransport<CentralBluetoothState> {
    /**
     * Stops the BLE session, optionally sending a session end command first.
     */
    suspend fun stop()
}
