package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeAdvertiser

/**
 * Provides a contract for accessing the device's [BluetoothAdapter].
 *
 * Decouples other components from the static Android `BluetoothManager` and
 * `BluetoothAdapter` classes.
 */
internal interface BluetoothAdapterProvider {
    /**
     * Checks if the Bluetooth adapter is currently enabled.
     */
    fun isEnabled(): Boolean

    /**
     * Retrieves the [BluetoothLeAdvertiser] associated with the Bluetooth adapter.
     *
     * @return The [BluetoothLeAdvertiser] if the adapter supports BLE advertising, or `null`
     * otherwise.
     */
    fun getAdvertiser(): BluetoothLeAdvertiser?
}
