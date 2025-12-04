package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.le.BluetoothLeAdvertiser

/**
 * Provides a contract for accessing the device's [android.bluetooth.BluetoothAdapter].
 *
 * Decouples other components from the static Android `BluetoothManager` and
 * `BluetoothAdapter` classes.
 */
interface BluetoothAdapterProvider {
    /**
     * Checks if the Bluetooth adapter is currently enabled.
     */
    fun isEnabled(): Boolean

    /**
     * Retrieves the [android.bluetooth.le.BluetoothLeAdvertiser] associated with the Bluetooth adapter.
     *
     * @return The [android.bluetooth.le.BluetoothLeAdvertiser] if the adapter supports BLE advertising, or `null`
     * otherwise.
     */
    fun getAdvertiser(): BluetoothLeAdvertiser?
}
