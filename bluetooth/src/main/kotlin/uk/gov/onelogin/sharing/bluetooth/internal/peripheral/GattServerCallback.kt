package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService

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

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        println("onCharacteristicWriteRequest")
        println("Device: ${device?.address}")
        println("RequestId: $requestId")
        println("Characteristic UUID: ${characteristic?.uuid}")
        println("PreparedWrite: $preparedWrite")
        println("ResponseNeeded: $responseNeeded")
        println("Offset: $offset")

        val state = value?.firstOrNull()?.let {
            MdocState.fromByte(it)
        }

        when (state) {
            MdocState.START -> {
                println("Received START command from ${device?.address}")
                gatGattEventEmitter.emit(GattEvent.ConnectionStateStarted)
            }

            null -> {
                println(
                    "Unknown or empty command: ${
                        value?.joinToString {
                            "%02X".format(it)
                        }
                    }"
                )
            }
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        gatGattEventEmitter.emit(
            GattEvent.ServiceAdded(status, service)
        )
    }
}
