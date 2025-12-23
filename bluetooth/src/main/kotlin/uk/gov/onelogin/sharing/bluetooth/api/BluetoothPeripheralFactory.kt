package uk.gov.onelogin.sharing.bluetooth.api

import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager

/**
 * A factory interface for creating the components required for a Bluetooth Peripheral role.
 *
 * This interface abstracts the creation of the [BleAdvertiser], [GattServerManager], and
 * [BluetoothStateMonitor]
 */
fun interface BluetoothPeripheralFactory {
    /**
     * Creates and returns a [BluetoothPeripheralComponents] instance containing the necessary
     * components for a Bluetooth Peripheral.
     *
     * @return A [BluetoothPeripheralComponents] object holding the configured advertiser,
     * server manager, and bluetooth state monitor.
     */
    fun create(): BluetoothPeripheralComponents
}

/**
 * A container data class for the components required to run a Bluetooth Peripheral.
 *
 * @param advertiser BLE advertiser.
 * @param gattServerManager GATT server manager.
 * @param bluetoothStateMonitor Bluetooth adapter state monitor.
 */
data class BluetoothPeripheralComponents(
    val advertiser: BleAdvertiser,
    val gattServerManager: GattServerManager,
    val bluetoothStateMonitor: BluetoothStateMonitor
)
