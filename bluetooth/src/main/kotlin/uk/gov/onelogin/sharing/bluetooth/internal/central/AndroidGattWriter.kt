package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class AndroidGattWriter : GattWriter {
    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        gatt.writeCharacteristic(
            characteristic,
            value,
            BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        ) == BluetoothStatusCodes.SUCCESS
    } else {
        characteristic.value = value
        gatt.writeCharacteristic(characteristic)
    }

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun notifyAndWriteToClientCharacteristic(
        server: BluetoothGattServer,
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        server.notifyCharacteristicChanged(
            device,
            characteristic,
            false,
            value
        ) == BluetoothStatusCodes.SUCCESS
    } else {
        characteristic.value = value
        server.notifyCharacteristicChanged(device, characteristic, false)
    }
}
