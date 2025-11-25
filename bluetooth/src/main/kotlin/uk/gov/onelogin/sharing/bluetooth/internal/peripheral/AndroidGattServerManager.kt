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
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec

class AndroidGattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val gattService: BluetoothGattService = AndroidGattServiceBuilder.build(
        GattServiceSpec.mdocService()
    )
) : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>(
        extraBufferCapacity = 32 // queue events if consumer is slow
    )
    override val events: SharedFlow<GattServerEvent> = _events
    private var gattServer: BluetoothGattServer? = null
    private val eventEmitter = GattEventEmitter {
        handleGattEvent(it)
    }

    override fun open() {
        val server = bluetoothManager.openGattServer(
            context,
            GattServerCallback(eventEmitter)
        )

        if (server == null) {
            _events.tryEmit(GattServerEvent.Error(MdocError.GATT_NOT_AVAILABLE))
            return
        }

        server.clearServices()
        server.addService(gattService)

        gattServer = server
    }

    override fun close() {
        gattServer?.close()
        gattServer = null
    }

    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.Error -> println(event.message)

            is GattEvent.ConnectionStateChange -> {
                val address = event.device.address

                when {
                    event.status == BluetoothGatt.GATT_SUCCESS &&
                            event.newState == BluetoothProfile.STATE_CONNECTED -> {

                        _events.tryEmit(GattServerEvent.Connected(address))
                    }

                    event.newState == BluetoothProfile.STATE_DISCONNECTED -> {
                        _events.tryEmit(GattServerEvent.Disconnected(address))

                    }

                    else -> {
                        _events.tryEmit(
                            GattServerEvent.UnsupportedEvent(
                                event.device.address,
                                event.status,
                                event.newState
                            )
                        )
                    }
                }
            }
        }
    }
}
