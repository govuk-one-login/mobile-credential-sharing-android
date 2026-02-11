package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer

class FakeGattWriter(val success: Boolean = true) : GattWriter {
    var writes = 0

    override fun writeCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean {
        writes++
        return success
    }

    override fun notifyAndWriteToClientCharacteristic(
        server: BluetoothGattServer,
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean = true
}
