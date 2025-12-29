package uk.gov.onelogin.sharing.bluetooth.api

import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager

/**
 * A factory interface for creating the components required for a Bluetooth Central role.
 *
 * This interface abstracts the creation of the [GattClientManager] and [BluetoothStateMonitor]
 */
fun interface BluetoothCentralFactory {
    /**
     * Creates and returns a [BluetoothCentralComponents]
     *
     * @return A [BluetoothCentralComponents] object holding the configured client
     * manager and state monitor.
     */
    fun create(): BluetoothCentralComponents
}

/**
 * A container data class for the components required to run a Bluetooth Central.
 *
 * @param gattClientManager GATT client manager.
 * @param bluetoothStateMonitor Bluetooth adapter state monitor.
 */
data class BluetoothCentralComponents(
    val gattClientManager: GattClientManager,
    val bluetoothStateMonitor: BluetoothStateMonitor
)
