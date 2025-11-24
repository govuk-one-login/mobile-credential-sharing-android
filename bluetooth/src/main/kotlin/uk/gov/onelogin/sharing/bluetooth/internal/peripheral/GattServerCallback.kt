package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServerCallback

internal class GattServerCallback(private val gatGattEventEmitter: GattEventEmitter) :
    BluetoothGattServerCallback() {
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        println("Address: ${device.address}")
        println("Status: $status")
        println("NewState: $newState")
        gatGattEventEmitter.emit(
            GattEvent.ConnectionStateChange(
                status,
                newState,
                device
            )
        )
    }
}