package uk.gov.onelogin.sharing.bluetooth.ble

import uk.gov.onelogin.sharing.bluetooth.api.permissions.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BluetoothAdvertiserProvider

class FakeBluetoothAdvertiserProvider : BluetoothAdvertiserProvider {
    var startCalled = 0
    var stopCalled = 0
    var parameters: AdvertisingParameters? = null
    var bleAdvertiseData: BleAdvertiseData? = null
    var callback: AdvertisingCallback? = null

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        startCalled++
        this.parameters = parameters
        this.bleAdvertiseData = bleAdvertiseData
        this.callback = callback
    }

    override fun stopAdvertisingSet() {
        stopCalled++
    }
}
