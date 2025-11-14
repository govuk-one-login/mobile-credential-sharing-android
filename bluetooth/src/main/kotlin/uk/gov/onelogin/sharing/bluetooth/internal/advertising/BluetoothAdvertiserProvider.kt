package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData

/**
 * Provides a contract for starting and stopping Bluetooth advertising.
 *
 * Decouples the [AndroidBleAdvertiser] from the concrete Android implementation
 * details.
 */
interface BluetoothAdvertiserProvider {
    /**
     * Starts a BLE advertising with the given parameters and data.
     *
     * @param parameters The [AdvertisingParameters] for the advertising set.
     * @param bleAdvertiseData The [BleAdvertiseData] to be advertised.
     * @param callback The [AdvertisingCallback] to be notified of the advertising status.
     */
    fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    /**
     * Stops the currently active advertising set.
     */
    fun stopAdvertisingSet()
}
