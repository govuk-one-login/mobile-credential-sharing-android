package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

internal interface GattEvent {
    data class ConnectionStateChange(val gatt: BluetoothGatt, val status: Int, val newState: Int) :
        GattEvent

    data class ServicesDiscovered(val gatt: BluetoothGatt, val status: Int) : GattEvent

    data class MtuChange(val gatt: BluetoothGatt, val mtu: Int, val status: Int) : GattEvent

    data class CharacteristicWrite(
        val gatt: BluetoothGatt,
        val characteristic: BluetoothGattCharacteristic,
        val status: Int
    ) : GattEvent
}
