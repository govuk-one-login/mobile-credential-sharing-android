package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.MdocError
import uk.gov.onelogin.sharing.bluetooth.api.MdocEvent
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec

class AndroidGattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val gattService: BluetoothGattService = AndroidGattServiceBuilder.build(
        GattServiceSpec.mdocService()
    )
) : GattServerManager {
    private val _events = MutableSharedFlow<MdocEvent>(
        extraBufferCapacity = 32 // queue events if consumer is slow
    )
    override val events: SharedFlow<MdocEvent> = _events
    private var gattServer: BluetoothGattServer? = null
    private val connectedDevices = mutableSetOf<String>()
    private val eventEmitter = GattEventEmitter {
        handleGattEvent(it)
    }

    override fun open() {
        val server = bluetoothManager.openGattServer(
            context,
            GattServerCallback(eventEmitter)
        )

        if (server == null) {
            _events.tryEmit(MdocEvent.Error(MdocError.GATT_NOT_AVAILABLE))
            return
        }

        server.clearServices()
        server.addService(gattService)

        gattServer = server
    }

    override fun close() {
        gattServer?.close()
        gattServer = null
        connectedDevices.clear()
    }

    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.Error -> println(event.message)

            is GattEvent.ConnectionStateChange -> {
                val address = event.device.address

                when {
                    event.status == BluetoothGatt.GATT_SUCCESS &&
                            event.newState == BluetoothProfile.STATE_CONNECTED -> {

                        if (connectedDevices.add(address)) {
                            _events.tryEmit(MdocEvent.Connected(address))
                        }
                    }

                    event.newState == BluetoothProfile.STATE_DISCONNECTED -> {
                        if (connectedDevices.remove(address)) {
                            _events.tryEmit(MdocEvent.Disconnected(address))
                        }
                    }

                    else -> {
                        _events.tryEmit(MdocEvent.UnsupportedEvent(
                            event.device.address,
                            event.status,
                            event.newState
                        ))
                    }
                }
            }
        }
    }
}
