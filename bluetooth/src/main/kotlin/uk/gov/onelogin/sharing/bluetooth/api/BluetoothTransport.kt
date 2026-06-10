package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for transferring data via Bluetooth.
 *
 * Exposes the implementation's [state] via [StateFlow].
 *
 * Begin the
 */
interface BluetoothTransport<State> {
    /**
     * The current state of the BLE session, exposed as a [kotlinx.coroutines.flow.StateFlow].
     */
    val state: StateFlow<State>

    /**
     * Starts the Bluetooth transport.
     *
     * @sample uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.AndroidCentralBluetoothTransport.start
     * @sample uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.AndroidPeripheralBluetoothTransport.start
     */
    suspend fun start(serviceUuid: UUID)
}