package uk.gov.onelogin.sharing.holder

import uk.gov.onelogin.sharing.bluetooth.api.BluetoothPeripheralComponents
import uk.gov.onelogin.sharing.bluetooth.api.BluetoothPeripheralFactory
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattServerManager

class FakeBluetoothPeripheralFactory(
    private val advertiser: BleAdvertiser = FakeBleAdvertiser(),
    private val gattServerManager: GattServerManager = FakeGattServerManager(),
    private val stateMonitor: BluetoothStateMonitor = FakeBluetoothStateMonitor()
) : BluetoothPeripheralFactory {

    override fun create(): BluetoothPeripheralComponents = BluetoothPeripheralComponents(
        advertiser = advertiser,
        gattServerManager = gattServerManager,
        bluetoothStateMonitor = stateMonitor
    )
}
