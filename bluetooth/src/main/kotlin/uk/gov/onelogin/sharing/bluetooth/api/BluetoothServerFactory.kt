package uk.gov.onelogin.sharing.bluetooth.api

import kotlinx.coroutines.CoroutineScope
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerManager

fun interface BluetoothServerFactory {
    fun createServer(scope: CoroutineScope): BluetoothServerComponents
}

data class BluetoothServerComponents(
    val advertiser: BleAdvertiser,
    val gattServer: GattServerManager,
    val bluetoothStateMonitor: BluetoothStateMonitor
)
