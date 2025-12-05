package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic

internal class GattClientCallback(private val gattEventEmitter: GattClientEventEmitter) :
    BluetoothGattCallback() {

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
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

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
    }
}
