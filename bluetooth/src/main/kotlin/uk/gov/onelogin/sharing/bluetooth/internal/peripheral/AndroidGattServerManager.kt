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
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerManager
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattUuids.STATE_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.MessageReassembler
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues.MIN_MTU
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates.SUCCESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.sendChunkedMessage
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceSpec
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.prerequisites.api.permissions.BluetoothPermissions.getBluetoothPermissions
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker

@ContributesBinding(AppScope::class)
@Suppress("TooManyFunctions", "LongParameterList")
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
    private val gattWriter: GattWriter,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
    reassemblerFactory: MessageReassembler.Factory,
    maxReceiveBufferSize: Int = DEFAULT_MAX_RECEIVE_BUFFER_SIZE
) : GattServerManager {
    private val _events = MutableSharedFlow<GattServerEvent>(
        extraBufferCapacity = 32 // queue events if consumer is slow
    )
    override val events: SharedFlow<GattServerEvent> = _events

    @Volatile
    private var gattServer: BluetoothGattServer? = null

    @Volatile
    internal var connectedDevice: BluetoothDevice? = null

    @Volatile
    private var serviceUuid: UUID? = null

    @SuppressLint("MissingPermission")
    private val eventEmitter = GattServerEventEmitter {
        handleGattEvent(it)
    }

    @Volatile
    private var mtu = MIN_MTU

    @Volatile
    private var isSessionEnd = false

    @Volatile
    private var isTerminating = false

    private val reassembler: MessageReassembler =
        reassemblerFactory.create(maxReceiveBufferSize)

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun open(serviceUuid: UUID) {
        val gattService = gattServiceFactory(serviceUuid)
        this.serviceUuid = serviceUuid
        isTerminating = false
        reassembler.clear()

        if (permissionsChecker.checkPermissions(getBluetoothPermissions()).isNotEmpty()) {
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
        serviceUuid = null
        isSessionEnd = false
        mtu = MIN_MTU
        _events.tryEmit(GattServerEvent.ServiceStopped)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleGattEvent(event: GattServerCallbackEvent) {
        when (event) {
            is GattServerCallbackEvent.ConnectionStateChange -> handleConnectionStateChange(event)

            is GattServerCallbackEvent.ServiceAdded -> handleServiceAdded(event)

            is GattServerCallbackEvent.CharacteristicWriteRequest ->
                handleCharacteristicWriteRequest(event)

            is GattServerCallbackEvent.MtuChanged -> mtu = event.mtu

            is GattServerCallbackEvent.DescriptorWriteRequest -> handleDescriptorWriteRequest(event)
        }
    }

    /**
     * Interprets a raw characteristic write: STATE writes become session signals and data writes
     * are passed to the reassembler, mirroring the central's [AndroidGattClientManager].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleCharacteristicWriteRequest(
        event: GattServerCallbackEvent.CharacteristicWriteRequest
    ) {
        if (isTerminating) return

        when (event.characteristicUuid) {
            STATE_UUID -> handleStateUpdate(event.device, event.value)
            else -> handleDataWrite(event)
        }
    }

    private fun handleStateUpdate(device: BluetoothDevice, value: ByteArray) {
        when (value.firstOrNull()?.let { MdocState.fromByte(it) }) {
            MdocState.START -> {
                logger.debug(logTag, "Received START command from ${device.address}")
                handleConnectionStateStarted()
            }

            MdocState.END -> {
                logger.debug(logTag, "GATT: Received Write Request 0x02 on State")
                handleSessionEndReceived()
            }

            null -> logger.debug(logTag, "Unknown or empty command on State characteristic")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleDataWrite(event: GattServerCallbackEvent.CharacteristicWriteRequest) {
        if (connectedDevice == null || event.device.address != connectedDevice?.address) {
            logger.debug(
                logTag,
                "Ignoring message from ${event.device.address} - not the accepted device"
            )
            return
        }

        when (val result = reassembler.accept(event.characteristicUuid, event.value)) {
            is MessageReassembler.Result.Incomplete ->
                logger.debug(logTag, "received message part, expecting additional data")

            is MessageReassembler.Result.ExceededMaxBuffer ->
                handleExceededMaxBufferSize()

            is MessageReassembler.Result.Complete ->
                _events.tryEmit(GattServerEvent.MessageReceived(result.message))

            is MessageReassembler.Result.InvalidPrefix ->
                logger.error(logTag, "invalid message received from remote device")
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancelCurrentConnection() {
        connectedDevice?.let {
            gattServer?.cancelConnection(it)
            connectedDevice = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleConnectionStateChange(event: GattServerCallbackEvent.ConnectionStateChange) {
        val address = event.device.address

        val event = when {
            event.status == BluetoothGatt.GATT_SUCCESS &&
                event.newState == BluetoothProfile.STATE_CONNECTED -> {
                connectedDevice = event.device
                GattServerEvent.Connected(event.device.address)
            }

            event.newState == BluetoothProfile.STATE_DISCONNECTED -> {
                // Suppress disconnection events for connections we never accepted.
                // This occurs when cancelConnection() is called on a device that connected before
                // the service was ready — Android still fires STATE_DISCONNECTED for the
                // cancelled connection, which would propagate as a false session failure event.
                if (connectedDevice == null) {
                    logger.debug(
                        logTag,
                        "Ignoring disconnection from $address - no accepted connection"
                    )
                    return
                }
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
        if (event.status == BluetoothGatt.GATT_SUCCESS) {
            _events.tryEmit(
                GattServerEvent.ServiceAdded(event.service)
            )
        } else {
            logger.error(logTag, "Failed to add service, status: ${event.status}")
            _events.tryEmit(
                GattServerEvent.Error(GattServerError.SERVICE_REGISTRATION_FAILED)
            )
        }
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
     * Handles when the accumulated BLE buffer exceeds the configured maximum size.
     * Sends the session end command to the Verifier, then closes the connection
     * and emits an error to trigger session failure and destruction.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleExceededMaxBufferSize() {
        isTerminating = true
        coroutineScope.launch {
            serviceUuid?.let { notifySessionEnd(it) }
            delay(BLE_SEND_NOTIFICATION_DELAY.milliseconds)
            cancelCurrentConnection()
            _events.tryEmit(GattServerEvent.Error(GattServerError.EXCEEDED_MAX_BUFFER_SIZE))
        }
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean {
        val server = gattServer ?: return false
        val device = connectedDevice ?: return false
        val characteristic = server.getService(serviceUuid)
            ?.getCharacteristic(SERVER_2_CLIENT_UUID) ?: return false

        return sendChunkedMessage(data, mtu, logger) { chunk ->
            gattWriter.notifyAndWriteToClientCharacteristic(
                server,
                device,
                characteristic,
                chunk
            )
        }
    }

    companion object {
        /** Default maximum receive buffer size for the holder role. */
        const val DEFAULT_MAX_RECEIVE_BUFFER_SIZE: Int = 64 * 1024
    }
}
