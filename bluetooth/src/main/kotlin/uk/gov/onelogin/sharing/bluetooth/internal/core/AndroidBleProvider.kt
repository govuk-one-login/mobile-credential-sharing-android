package uk.gov.onelogin.sharing.bluetooth.internal.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BluetoothAdvertiserProvider

@ContributesBinding(AppScope::class)
class AndroidBleProvider(
    private val bluetoothAdapter: BluetoothAdapterProvider,
    private val bleAdvertiser: BluetoothAdvertiserProvider
) : BleProvider {

    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled()

    override fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        bleAdvertiser.startAdvertisingSet(
            parameters,
            bleAdvertiseData,
            callback
        )
    }

    override fun stopAdvertising() {
        bleAdvertiser.stopAdvertisingSet()
    }
}
