package uk.gov.onelogin.sharing.bluetooth.internal.core

import kotlinx.coroutines.flow.Flow

/**
 * Monitors the state of the device's Bluetooth adapter.
 *
 * This contract is responsible for observing changes to the Bluetooth state
 * and exposing them as a [Flow] of [BluetoothStatus] events.
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
