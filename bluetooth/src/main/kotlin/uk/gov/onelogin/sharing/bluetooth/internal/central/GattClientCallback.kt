package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback

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
}
