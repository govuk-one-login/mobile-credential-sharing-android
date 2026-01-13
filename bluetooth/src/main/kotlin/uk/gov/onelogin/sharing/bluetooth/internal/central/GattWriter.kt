package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

/**
 * Abstracts the write operation for a [BluetoothGattCharacteristic].
 *
 * This is necessary because the Android SDK has different implementation for
 * writing characteristics in older and newer SDK versions.
 */
fun interface GattWriter {
    /**
     * Writes a value to a given characteristic using the provided GATT instance.
     *
     * @param gatt The [BluetoothGatt] instance to perform the write operation.
     * @param characteristic The [BluetoothGattCharacteristic] to be updated.
     * @param value The byte array to write to the characteristic.
     * @return `true` if the write operation was initiated successfully, `false` otherwise.
     */
    fun writeCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean
}
