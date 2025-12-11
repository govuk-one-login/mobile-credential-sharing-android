package uk.gov.onelogin.sharing.bluetooth.api.core

import kotlinx.coroutines.flow.Flow

/**
 * Monitors the state of the device's Bluetooth adapter.
 *
 * Observes changes to the Bluetooth state changes and exposes them
 * as a [Flow] of [BluetoothStatus] events.
 */
interface BluetoothStateMonitor {
    /**
     * A [Flow] that emits [BluetoothStatus] updates whenever the Bluetooth adapter's state changes.
     */
    val states: Flow<BluetoothStatus>

    /**
     * Starts monitoring the Bluetooth adapter state.
     */
    fun start()

    /**
     * Stops monitoring the Bluetooth adapter state.
     */
    fun stop()
}
