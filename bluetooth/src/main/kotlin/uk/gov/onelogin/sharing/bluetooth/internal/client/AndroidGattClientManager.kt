package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState

class AndroidGattClientManager(
    private val context: Context,
) : GattClientManager {
    private val _events = MutableSharedFlow<GattClientEvent>()
    override val events: SharedFlow<GattClientEvent> = _events

    private var gatt: BluetoothGatt? = null
    private val eventEmitter = GattClientEventEmitter{
        handleGattEvent(it)
    }

    override fun connect(device: BluetoothDevice) {
        gatt = device.connectGatt(
            context,
            false,
            GattClientCallback(eventEmitter)
        )
    }

    override fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    override fun writeState(command: MdocState) {
        TODO("Not yet implemented")
    }

    private fun handleGattEvent(event: GattEvent) {
        when(event) {
            is GattEvent.ConnectionStateChange -> {

            }
            is GattEvent.ServicesDiscovered -> {

            }
        }
    }
}