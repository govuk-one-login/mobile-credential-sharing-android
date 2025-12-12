package uk.gov.onelogin.sharing.bluetooth.api

import kotlinx.coroutines.CoroutineScope
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager

fun interface BluetoothPeripheralFactory {
    fun create(scope: CoroutineScope): BluetoothPeripheralComponents
}

data class BluetoothPeripheralComponents(
    val advertiser: BleAdvertiser,
    val gattServerManager: GattServerManager,
    val bluetoothStateMonitor: BluetoothStateMonitor
)
