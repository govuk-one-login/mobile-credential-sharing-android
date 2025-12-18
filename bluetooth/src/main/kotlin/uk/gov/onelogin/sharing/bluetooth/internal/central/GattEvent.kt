package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent

internal interface GattEvent {
    data class ConnectionStateChange(val gatt: BluetoothGatt, val status: Int, val newState: Int) :
        GattEvent

    data class ServicesDiscovered(val bluetoothGatt: BluetoothGatt, val status: Int) : GattEvent
}
