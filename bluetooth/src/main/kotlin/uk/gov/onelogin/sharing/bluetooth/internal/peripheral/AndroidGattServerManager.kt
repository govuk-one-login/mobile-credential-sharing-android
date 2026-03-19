package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallbackEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.STATE_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues.MIN_MTU
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.NOTIFY_CLIENT_FAILED
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.SUCCESS
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(AppScope::class)
class AndroidGattServerManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val gattServiceFactory: (UUID) -> BluetoothGattService = {
        AndroidGattServiceBuilder.build(
            GattServiceSpec.mdocService(it)
        )
    },
    private val permissionsChecker: BluetoothPermissionChecker,
    private val logger: Logger,
    private val gattWriter: GattWriter
) : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>(
        extraBufferCapacity = 32 // queue events if consumer is slow
    )
    override val events: SharedFlow<GattServerEvent> = _events
    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null

    @SuppressLint("MissingPermission")
    private val eventEmitter = GattEventEmitter {
        handleGattEvent(it)
    }
    private var mtu = MIN_MTU
    private var isSessionEnd = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun open(serviceUuid: UUID) {
        val gattService = gattServiceFactory(serviceUuid)

        if (!permissionsChecker.hasBluetoothPermissions()) {
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
        connectedDevice = null
        isSessionEnd = false
        mtu = MIN_MTU
        _events.tryEmit(GattServerEvent.ServiceStopped)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleGattEvent(event: GattServerCallbackEvent) {
        when (event) {
            is GattServerCallbackEvent.ConnectionStateChange -> handleConnectionStateChange(event)
            is GattServerCallbackEvent.ConnectionStateStarted -> handleConnectionStateStarted()
            is GattServerCallbackEvent.ServiceAdded -> handleServiceAdded(event)
            is GattServerCallbackEvent.MessageReceived -> handleMessageReceived(event)
            is GattServerCallbackEvent.MtuChanged -> mtu = event.mtu
            is GattServerCallbackEvent.DescriptorWriteRequest -> handleDescriptorWriteRequest(event)
            is GattServerCallbackEvent.SessionEnd -> handleSessionEndReceived()
        }
    }

    private fun handleMessageReceived(event: GattServerCallbackEvent.MessageReceived) {
        _events.tryEmit(GattServerEvent.MessageReceived(event.byteArray))
    }

    private fun handleConnectionStateChange(event: GattServerCallbackEvent.ConnectionStateChange) {
        val address = event.device.address

        val event = when {
            event.status == BluetoothGatt.GATT_SUCCESS &&
                event.newState == BluetoothProfile.STATE_CONNECTED -> {
                connectedDevice = event.device
                GattServerEvent.Connected(address)
            }

            event.newState == BluetoothProfile.STATE_DISCONNECTED -> {
                connectedDevice = null
                GattServerEvent.Disconnected(address, isSessionEnd)
            }

            else -> GattServerEvent.UnsupportedEvent(
                event.device.address,
                event.status,
                event.newState
            )
        }

        _events.tryEmit(event)
    }

    private fun handleServiceAdded(event: GattServerCallbackEvent.ServiceAdded) {
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
    private fun handleDescriptorWriteRequest(
        event: GattServerCallbackEvent.DescriptorWriteRequest
    ) {
        when (event) {
            is GattServerCallbackEvent.DescriptorWriteRequest.Invalid -> {
                _events.tryEmit(
                    GattServerEvent.Error(
                        GattServerError.DESCRIPTOR_WRITE_REQUEST_FAILED
                    )
                )
                logger.error(logTag, "Invalid descriptor write request: ${event.reason}")
            }

            is GattServerCallbackEvent.DescriptorWriteRequest.Valid ->
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
     *  To handle when the holder successfully receives a END command from the reader.
     * [isSessionEnd] is set to true as a END command has been successfully been received and helps
     * differentiate between a graceful teardown and a forced disconnection when an error occurs.
     */
    private fun handleSessionEndReceived() {
        isSessionEnd = true
        _events.tryEmit(GattServerEvent.SessionEnd(SUCCESS))
    }

    /**
     * Notifies the reader device with the intent to end the session. Notifying can pass or fail
     * therefore we invoke [SUCCESS] or [NOTIFY_CLIENT_FAILED] if the holder fails to write to
     * the readers characteristic.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun notifySessionEnd(serviceUuid: UUID): SessionEndStateQueued {
        val server = gattServer ?: return SessionEndStateQueued.Failed
        val characteristic: BluetoothGattCharacteristic =
            server.getService(serviceUuid)?.getCharacteristic(STATE_UUID)
                ?: return SessionEndStateQueued.Failed
        val endValue = byteArrayOf(MdocState.END.code)

        val device = connectedDevice
        if (device == null) {
            logger.debug(logTag, "END command not sent - No device connected")
            return SessionEndStateQueued.NoDeviceConnected
        }

        val notificationResult = gattWriter.notifyAndWriteToClientCharacteristic(
            server,
            device,
            characteristic,
            endValue
        )

        return if (notificationResult) {
            logger.debug(logTag, "GATT: Notified state characteristic with 0x02")
            isSessionEnd = true
            SessionEndStateQueued.Success
        } else {
            logger.error(logTag, "failed to notify client with END command: notification failed")
            SessionEndStateQueued.Failed
        }
    }
}
