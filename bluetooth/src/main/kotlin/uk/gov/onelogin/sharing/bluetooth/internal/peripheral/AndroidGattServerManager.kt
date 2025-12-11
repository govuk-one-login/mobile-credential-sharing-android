package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.Manifest
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.server.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec

class AndroidGattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val gattServiceFactory: (UUID) -> BluetoothGattService = {
        AndroidGattServiceBuilder.build(
            GattServiceSpec.mdocService(it)
        )
    },
    private val permissionsChecker: PermissionChecker,
    private val logger: Logger
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
    override fun open(serviceUuid: UUID) {
        val gattService = gattServiceFactory(serviceUuid)

        if (!permissionsChecker.hasPermission()) {
            _events.tryEmit(
                GattServerEvent.Error(
                    GattServerError.BLUETOOTH_PERMISSION_MISSING
                )
            )
            return
        }

        val server = bluetoothManager.openGattServer(
            context,
            GattServerCallback(
                gatGattEventEmitter = eventEmitter,
                logger = logger
            )
        )

        if (server == null) {
            _events.tryEmit(GattServerEvent.Error(GattServerError.GATT_NOT_AVAILABLE))
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
        _events.tryEmit(GattServerEvent.ServiceStopped)
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

            GattEvent.ConnectionStateStarted -> {
                _events.tryEmit(GattServerEvent.SessionStarted)
            }
        }
    }
}
