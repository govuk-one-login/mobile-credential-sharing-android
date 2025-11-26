package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.Manifest
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun open() {
        val server = bluetoothManager.openGattServer(
            context,
            GattServerCallback(eventEmitter)
        )

        if (server == null) {
            _events.tryEmit(GattServerEvent.Error(MdocSessionError.GATT_NOT_AVAILABLE))
            return
        }

        server.clearServices()
        server.addService(gattService)

        gattServer = server
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun close() {
        gattServer?.close()
        gattServer = null
    }

    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.ConnectionStateChange ->
                _events.tryEmit(event.toGattServerEvent())

            is GattEvent.ServiceAdded -> {
                _events.tryEmit(
                    GattServerEvent.ServiceAdded(
                        event.status,
                        event.service
                    )
                )
            }
        }
    }
}
