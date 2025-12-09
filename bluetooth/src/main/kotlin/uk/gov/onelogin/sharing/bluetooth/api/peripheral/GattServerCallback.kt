package uk.gov.onelogin.sharing.bluetooth.api.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEvent

class GattServerCallback(private val gatGattEventEmitter: GattEventEmitter) :
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

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        gatGattEventEmitter.emit(
            GattEvent.ServiceAdded(status, service)
        )
    }
}
