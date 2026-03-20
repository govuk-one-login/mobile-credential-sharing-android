package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

/**
 * Responsible for orchestrating BLE scanning and GATT client connection.
 *
 * Exposes [CentralBluetoothState] via a [StateFlow].
 */
interface CentralBluetoothTransport {
    /**
     * The current state of the BLE session, exposed as a [StateFlow].
     */
    val state: StateFlow<CentralBluetoothState>

    /**
     * Current state of the Bluetooth adapter, exposed as a [StateFlow].
     */
    val bluetoothStatus: StateFlow<BluetoothStatus>

    /**
     * Starts scanning for a peripheral advertising the given service UUID,
     * then connects when found.
     *
     * @param serviceUuid The service UUID bytes to scan for.
     */
    fun scanAndConnect(serviceUuid: ByteArray)

    /**
     * Stops the BLE session, optionally sending a session end command first.
     */
    suspend fun stop()
}
