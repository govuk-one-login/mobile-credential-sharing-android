package uk.gov.onelogin.sharing.bluetooth.api

import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager

fun interface BluetoothCentralFactory {
    fun create(): BluetoothCentralComponents
}

data class BluetoothCentralComponents(
    val gattClientManager: GattClientManager,
    val bluetoothStateMonitor: BluetoothStateMonitor
)
