package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.annotation.RequiresPermission
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues.MIN_MTU
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.NOTIFY_CLIENT_FAILED
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.SUCCESS
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec
import uk.gov.onelogin.sharing.core.logger.logTag

class AndroidGattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val gattServiceFactory: (UUID) -> BluetoothGattService = {
        AndroidGattServiceBuilder.build(
            GattServiceSpec.mdocService(it)
        )
    },
    private val permissionsChecker: PermissionChecker,
    private val logger: Logger,
    private val gattWriter: GattWriter
) : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>(
        extraBufferCapacity = 32 // queue events if consumer is slow
    )
    override val events: SharedFlow<GattServerEvent> = _events
    private var gattServer: BluetoothGattServer? = null

    @SuppressLint("MissingPermission")
    private val eventEmitter = GattEventEmitter {
        handleGattEvent(it)
    }
    private var mtu = MIN_MTU

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun open(serviceUuid: UUID) {
        val gattService = gattServiceFactory(serviceUuid)

        if (!permissionsChecker.hasPeripheralPermissions()) {
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

        logger.debug(logTag, "starting server: $server")

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.ConnectionStateChange -> handleConnectionStateChange(event)
            is GattEvent.ConnectionStateStarted -> handleConnectionStateStarted()
            is GattEvent.ServiceAdded -> handleServiceAdded(event)
            is GattEvent.MessageReceived -> Unit
            is GattEvent.MtuChanged -> mtu = event.mtu
            is GattEvent.DescriptorWriteRequest -> handleDescriptorWriteRequest(event)
            is GattEvent.SessionEnd -> handleSessionEndReceived()
        }
    }

    private fun handleConnectionStateChange(event: GattEvent.ConnectionStateChange) {
        _events.tryEmit(event.toGattServerEvent())
    }

    private fun handleServiceAdded(event: GattEvent.ServiceAdded) {
        _events.tryEmit(
            GattServerEvent.ServiceAdded(
                event.status,
                event.service
            )
        )
    }

    private fun handleConnectionStateStarted() {
        // state characteristic was set to `start` by the remote device
        _events.tryEmit(GattServerEvent.SessionStarted)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleDescriptorWriteRequest(event: GattEvent.DescriptorWriteRequest) {
        when (event) {
            is GattEvent.DescriptorWriteRequest.Invalid -> {
                _events.tryEmit(
                    GattServerEvent.Error(
                        GattServerError.DESCRIPTOR_WRITE_REQUEST_FAILED
                    )
                )
                logger.error(logTag, "Invalid descriptor write request: ${event.reason}")
            }

            is GattEvent.DescriptorWriteRequest.Valid ->
                if (event.responseNeeded) {
                    gattServer?.sendResponse(
                        event.device,
                        event.requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        event.offset,
                        event.value
                    )
                } else {
                    logger.debug(
                        logTag,
                        "Received descriptor write requests " +
                            "- response not needed"
                    )
                }
        }
    }

    /**
     * To handle when the holder successfully receives a END command from the reader.
     */
    private fun handleSessionEndReceived() {
        _events.tryEmit(GattServerEvent.SessionEnd(SUCCESS))
    }

    /**
     * Notifies the reader device with the intent to end the session. Notifying can pass or fail
     * therefore we invoke [SUCCESS] or [NOTIFY_CLIENT_FAILED] if the holder fails to write to
     * the readers characteristic.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun notifySessionEnd(serviceUuid: UUID) {
        val server = gattServer ?: return
        val characteristic =
            server.getService(serviceUuid)?.getCharacteristic(SERVER_2_CLIENT_UUID) ?: return
        val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        val endValue = byteArrayOf(MdocState.END.code)

        if (connectedDevices.isEmpty()) {
            logger.error(logTag, "failed to notify client with END command: No devices connected")
            _events.tryEmit(GattServerEvent.SessionEnd(NOTIFY_CLIENT_FAILED))
            return
        }

        connectedDevices.forEach { device ->
            val notificationResult =
                gattWriter.notifyAndWriteToClientCharacteristic(
                    server,
                    device,
                    characteristic,
                    endValue
                )

            val event =
                if (notificationResult) {
                    logger.debug(logTag, "GATT: Notified state characteristic with 0x02")
                    GattServerEvent.SessionEnd(SUCCESS)
                } else {
                    GattServerEvent.SessionEnd(NOTIFY_CLIENT_FAILED)
                }

            _events.tryEmit(event)
        }
    }
}
