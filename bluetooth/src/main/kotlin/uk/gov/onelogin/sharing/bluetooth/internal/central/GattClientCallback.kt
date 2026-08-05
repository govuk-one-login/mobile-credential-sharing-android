package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

internal class GattClientCallback(private val gattEventEmitter: GattClientEventEmitter) :
    BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        gattEventEmitter.emit(
            GattEvent.ConnectionStateChange(
                gatt,
                status,
                newState
            )
        )
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        gattEventEmitter.emit(
            GattEvent.ServicesDiscovered(
                gatt,
                status
            )
        )
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        gattEventEmitter.emit(
            GattEvent.MtuChange(
                gatt,
                mtu,
                status
            )
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        gattEventEmitter.emit(
            GattEvent.CharacteristicWrite(
                gatt,
                characteristic,
                status
            )
        )
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        gattEventEmitter.emit(
            GattEvent.CharacteristicChanged(
                gatt = gatt,
                characteristic = characteristic,
                value = value
            )
        )
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        gattEventEmitter.emit(
            GattEvent.DescriptorWrite(
                gatt = gatt,
                descriptor = descriptor,
                status = status
            )
        )
    }

    // Note: onServiceChanged requires API 31+.
    // On older devices the verifier will rely on the session inactivity timeout to recover.
    override fun onServiceChanged(gatt: BluetoothGatt) {
        gattEventEmitter.emit(GattEvent.ServiceChanged(gatt))
    }
}
