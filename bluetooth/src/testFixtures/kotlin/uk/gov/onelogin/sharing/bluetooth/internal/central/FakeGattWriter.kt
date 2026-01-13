package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

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
}
