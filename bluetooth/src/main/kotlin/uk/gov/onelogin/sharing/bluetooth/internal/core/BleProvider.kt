package uk.gov.onelogin.sharing.bluetooth.internal.core

import uk.gov.onelogin.sharing.bluetooth.api.permissions.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData

/**
 * Provides a contract for interacting with the device's Bluetooth Low
 * Energy (BLE) capabilities.
 */
internal interface BleProvider {
    /**
     * Checks if Bluetooth is currently enabled on the device.
     */
    fun isBluetoothEnabled(): Boolean

    /**
     * Starts BLE advertising with the given parameters and data.
     *
     * @param parameters The [AdvertisingParameters] to configure the advertisement.
     * @param bleAdvertiseData The [BleAdvertiseData] to be broadcast.
     * @param callback The [AdvertisingCallback] to be notified of advertising events.
     */
    fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    )

    /**
     * Stops active BLE advertising.
     */
    fun stopAdvertising()
}
