package uk.gov.onelogin.sharing.holder

import kotlinx.coroutines.CoroutineScope
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothServerComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothServerFactory
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattServerManager

class FakeBluetoothServerFactory(
    private val advertiser: BleAdvertiser = FakeBleAdvertiser(),
    private val gattServerManager: GattServerManager = FakeGattServerManager(),
    private val stateMonitor: BluetoothStateMonitor = FakeBluetoothStateMonitor()
) : BluetoothServerFactory {

    override fun createServer(scope: CoroutineScope): BluetoothServerComponents =
        BluetoothServerComponents(
            advertiser = advertiser,
            gattServer = gattServerManager,
            bluetoothStateMonitor = stateMonitor
        )
}
