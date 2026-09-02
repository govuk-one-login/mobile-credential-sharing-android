package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.NON_LAST_PART
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.STATE_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.BLE_SEND_NOTIFICATION_DELAY
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues.MIN_MTU
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.bluetooth.internal.core.sendChunkedMessage
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ValidationResult
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.prerequisites.api.permissions.BluetoothPermissions.getBluetoothPermissions
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker

const val INVALID_SERVICE = "Gatt Service does not have a state characteristic"

@ContributesBinding(AppScope::class)
@Suppress("TooManyFunctions", "LongParameterList")
class AndroidGattClientManager(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
    private val serviceValidator: ServiceValidator,
    private val gattWriter: GattWriter,
    private val logger: Logger,
    private val writeQueue: GattWriteQueue,
    @param:ApplicationScope private val coroutineScope: CoroutineScope,
    private val maxReceiveBufferSize: Int = DEFAULT_MAX_RECEIVE_BUFFER_SIZE
) : GattClientManager {
    private val _events = MutableSharedFlow<GattClientEvent>(
        extraBufferCapacity = 32
    )
    override val events: SharedFlow<GattClientEvent> = _events

    @Volatile
    private var bluetoothGatt: BluetoothGatt? = null

    @Volatile
    private var serviceUuid: UUID? = null
    private val eventEmitter = GattClientEventEmitter {
        handleGattEvent(it)
    }

    @Volatile
    private var mtu = MIN_MTU

    @Volatile
    private var isSessionEnd = false

    @Volatile
    private var isTerminating = false

    /**
     * Guards the one-time emission of [GattClientEvent.ConnectionStateStarted]. Readiness is
     * deferred until the START write is confirmed so the following SessionEstablishment write is
     * not submitted while START is still in flight (which the stack can reject as busy).
     */
    private val startSignalled = AtomicBoolean(false)

    @Volatile
    private var startTimeoutJob: Job? = null

    @Volatile
    private var awaitingStartConfirmation = false
    private val pendingDescriptorWrites = ArrayDeque<BluetoothGattDescriptor>()
    private val messages: MutableMap<UUID, ByteArray> = mutableMapOf()

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        if (permissionChecker.checkPermissions(getBluetoothPermissions()).isNotEmpty()) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                )
            )
            return
        }

        this.serviceUuid = serviceUuid
        pendingDescriptorWrites.clear()
        messages.clear()
        isTerminating = false
        isSessionEnd = false
        awaitingStartConfirmation = false
        startSignalled.set(false)
        _events.tryEmit(GattClientEvent.Connecting)

        bluetoothGatt = try {
            device.connectGatt(
                context,
                false,
                GattClientCallback(eventEmitter),
                BluetoothDevice.TRANSPORT_LE
            )
        } catch (e: SecurityException) {
            logger.error(logTag, "Security exception", e)
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                )
            )
            null
        }

        if (bluetoothGatt == null) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_GATT_NOT_AVAILABLE
                )
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        logger.debug(logTag, "Disconnect GATT client")
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun notifySessionEnd(): SessionEndStates {
        val gatt =
            bluetoothGatt ?: return SessionEndStates.WRITE_TO_SERVER_FAILED

        val state = gatt
            .getService(serviceUuid)
            ?.getCharacteristic(STATE_UUID) ?: return handleError(
            ClientError.INVALID_SERVICE,
            INVALID_SERVICE
        ).let { SessionEndStates.WRITE_TO_SERVER_FAILED }

        val endVal = byteArrayOf(MdocState.END.code)

        val writeSuccess = gattWriter.writeCharacteristic(
            gatt = gatt,
            characteristic = state,
            value = endVal
        )

        if (!writeSuccess) return SessionEndStates.WRITE_TO_SERVER_FAILED

        logger.debug(logTag, "GATT: Wrote 0x02 to State characteristic")
        logger.debug(
            logTag,
            "BLE session terminated successfully via GATT End command"
        )
        isSessionEnd = true

        return SessionEndStates.SUCCESS
    }

    private fun handleGattEvent(event: GattEvent) {
        try {
            when (event) {
                is GattEvent.ConnectionStateChange -> connectionChanged(event)
                is GattEvent.ServicesDiscovered -> servicesDiscovered(event)
                is GattEvent.MtuChange -> changedMtu(event)
                is GattEvent.CharacteristicWrite -> characteristicWritten(event)
                is GattEvent.CharacteristicChanged -> handleCharacteristicChanged(event)
                is GattEvent.DescriptorWrite -> descriptorWritten(event)
                is GattEvent.ServiceChanged -> handleServiceChanged()
            }
        } catch (e: SecurityException) {
            logger.error(logTag, "Security exception", e)
            _events.tryEmit(
                GattClientEvent.Error(ClientError.BLUETOOTH_PERMISSION_MISSING)
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean {
        val gatt = bluetoothGatt ?: return false
        val characteristic = gatt.getService(serviceUuid)
            ?.getCharacteristic(CLIENT_2_SERVER_UUID) ?: return false

        return sendChunkedMessage(data, mtu, logger) { chunk ->
            val written = gattWriter.writeCharacteristic(
                gatt = gatt,
                characteristic = characteristic,
                value = chunk
            )
            written && writeQueue.awaitWriteConfirmation()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectionChanged(event: GattEvent.ConnectionStateChange) {
        val address = event.gatt.device.address

        val clientEvent = when {
            event.status == BluetoothGatt.GATT_SUCCESS &&
                event.newState == BluetoothGatt.STATE_CONNECTED -> {
                bluetoothGatt = event.gatt

                bluetoothGatt?.discoverServices()

                GattClientEvent.Connected(address)
            }

            event.newState == BluetoothGatt.STATE_DISCONNECTED -> {
                bluetoothGatt?.close()
                bluetoothGatt = null
                GattClientEvent.Disconnected(address, isSessionEnd)
            }

            else -> GattClientEvent.UnsupportedEvent(
                address,
                event.status,
                event.newState
            )
        }

        _events.tryEmit(clientEvent)
    }

    /**
     * Begins the GATT connection setup sequence:
     * 1. [servicesDiscovered] enables notifications and queues CCCD descriptor writes.
     * 2. [changedMtu] triggers [writeNextDescriptor] to drain the queue.
     * 3. [descriptorWritten] chains through remaining descriptors; once all are
     *    written it calls [writeStartState] to signal the session is ready.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun servicesDiscovered(event: GattEvent.ServicesDiscovered) {
        logger.debug(logTag, "Services discovered: status=${event.status}")

        if (event.status != BluetoothGatt.GATT_SUCCESS) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.SERVICE_DISCOVERED_ERROR
                )
            )
            return
        }

        val service = event.gatt.getService(serviceUuid)
        if (service == null) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.SERVICE_NOT_FOUND
                )
            )
            return
        }

        if (serviceValidator.validate(service) is ValidationResult.Failure) {
            logger.debug(logTag, "Incompatible mDL service: missing characteristics")
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.INVALID_SERVICE
                )
            )
        } else {
            subscribeToCharacteristics(service)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun subscribeToCharacteristics(service: BluetoothGattService) {
        val gatt = bluetoothGatt.let { bluetoothGatt } ?: return

        // requests maximum but a lower MTU could be negotiated between devices
        // this is ignored in android >= 14 - It always requests 517
        val mtuRequestSuccess = gatt.requestMtu(MtuValues.MAX_MTU)
        logger.debug(logTag, "Request max MTU success: $mtuRequestSuccess")

        val state = service.getCharacteristic(GattUuids.STATE_UUID)
        val serverToClient = service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID)

        if (state == null || serverToClient == null) {
            handleError(ClientError.INVALID_SERVICE, INVALID_SERVICE)
            return
        }

        // Enable local notification listeners
        val success = gatt.setCharacteristicNotification(
            state,
            true
        ) && gatt.setCharacteristicNotification(serverToClient, true)

        if (!success) {
            handleError(
                ClientError.FAILED_TO_SUBSCRIBE,
                "Failed to subscribe to characteristics"
            )
            return
        }

        logger.debug(logTag, "Notifications enabled, checking CCCD descriptors")

        // Queue CCCD descriptor writes for after MTU negotiation completes.
        // setCharacteristicNotification only registers a local listener on Android.
        // The explicit CCCD write tells the remote peripheral to send notifications.
        // This is required for iOS interoperability — CoreBluetooth peripherals will
        // not emit notifications unless the CCCD descriptor has been written. Testing
        // with Android-only devices may mask this requirement.
        listOf(state, serverToClient).forEach { characteristic ->
            characteristic
                .getDescriptor(GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID)
                ?.let { pendingDescriptorWrites.addLast(it) }
        }
    }

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeNextDescriptor(gatt: BluetoothGatt) {
        val descriptor = pendingDescriptorWrites.removeFirstOrNull() ?: return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(
                descriptor,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            )
        } else {
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun descriptorWritten(event: GattEvent.DescriptorWrite) {
        if (event.status != BluetoothGatt.GATT_SUCCESS) {
            return handleError(
                ClientError.FAILED_TO_SUBSCRIBE,
                "Failed to write CCCD descriptor: status=${event.status}"
            )
        }
        logger.debug(logTag, "CCCD descriptor written for: ${event.descriptor.characteristic.uuid}")
        if (pendingDescriptorWrites.isNotEmpty()) {
            writeNextDescriptor(event.gatt)
        } else {
            logger.debug(logTag, "All CCCD descriptors written")
            writeStartState()
        }
    }

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun changedMtu(event: GattEvent.MtuChange) {
        logger.debug(logTag, "MTU negotiated: ${event.mtu}")
        mtu = event.mtu

        if (pendingDescriptorWrites.isNotEmpty()) {
            writeNextDescriptor(event.gatt)
        } else {
            writeStartState()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun writeStartState() {
        val gatt = bluetoothGatt ?: return
        val state = gatt
            .getService(serviceUuid)
            ?.getCharacteristic(GattUuids.STATE_UUID) ?: return handleError(
            ClientError.INVALID_SERVICE,
            INVALID_SERVICE
        )

        val startValue = byteArrayOf(MdocState.START.code)
        awaitingStartConfirmation = true
        startSignalled.set(false)
        val writeSuccess = gattWriter.writeCharacteristic(
            gatt = gatt,
            characteristic = state,
            value = startValue
        )

        if (!writeSuccess) {
            awaitingStartConfirmation = false
            handleError(
                ClientError.FAILED_TO_START,
                "Failed to write 'Start' state"
            )
            return
        }

        // Readiness is signalled once START is confirmed (see characteristicWritten); the
        // timeout fallback ensures setup proceeds if the confirmation never arrives.
        startTimeoutJob = coroutineScope.launch {
            delay(START_CONFIRMATION_TIMEOUT.milliseconds)
            if (awaitingStartConfirmation) {
                logger.debug(
                    logTag,
                    "START confirmation timed out; proceeding after fallback delay"
                )
                emitConnectionStarted()
            }
        }
    }

    private fun emitConnectionStarted() {
        awaitingStartConfirmation = false
        startTimeoutJob?.cancel()
        startTimeoutJob = null
        if (startSignalled.compareAndSet(false, true)) {
            logger.debug(logTag, "Connection state = STARTED")
            _events.tryEmit(GattClientEvent.ConnectionStateStarted)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun characteristicWritten(event: GattEvent.CharacteristicWrite) {
        val uuid = event.characteristic.uuid
        val isPendingStart = awaitingStartConfirmation && uuid == STATE_UUID

        if (event.status != BluetoothGatt.GATT_SUCCESS) {
            if (isPendingStart) {
                awaitingStartConfirmation = false
                startTimeoutJob?.cancel()
                startTimeoutJob = null
            } else {
                writeQueue.onWriteComplete(uuid, false)
            }
            return handleError(
                ClientError.FAILED_TO_START,
                "Failed to write 'Start' state"
            )
        }

        logger.debug(logTag, "Wrote value to characteristic: $uuid")

        // Signal readiness only after START is confirmed, pacing the SessionEstablishment write.
        if (isPendingStart) {
            emitConnectionStarted()
            return
        }

        writeQueue.onWriteComplete(uuid, true)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleServiceChanged() {
        if (isTerminating) return
        handleError(
            ClientError.SERVICE_CHANGED,
            "Remote GATT server services changed - session invalidated"
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleError(error: ClientError, reason: String) {
        logger.error(logTag, reason)

        _events.tryEmit(
            GattClientEvent.Error(
                error
            )
        )

        disconnect()
    }

    /**
     * Handles incoming notification changes from the central device.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleCharacteristicChanged(event: GattEvent.CharacteristicChanged) {
        if (isTerminating) return
        val firstByte = event.value?.firstOrNull() ?: return

        when (event.characteristic.uuid) {
            STATE_UUID -> {
                when (firstByte) {
                    MdocState.END.code -> {
                        logger.debug(logTag, "GATT: Received notification 0x02 on State")
                        isSessionEnd = true
                        bluetoothGatt?.disconnect()

                        GattClientEvent.SessionEnd(SessionEndStates.SUCCESS)
                    }

                    else -> {
                        // Currently do nothing with codes other than [END].
                        null
                    }
                }
            }

            SERVER_2_CLIENT_UUID -> {
                handleServerToClientMessage(event.characteristic, event.value, firstByte)
            }

            else -> {
                null
            }
        }?.let { _events.tryEmit(it) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleServerToClientMessage(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        firstByte: Byte
    ): GattClientEvent? {
        val previousMessages = messages[characteristic.uuid] ?: byteArrayOf()
        val messageBytes = value.drop(1).toByteArray()

        return when (firstByte) {
            NON_LAST_PART -> {
                val accumulated = previousMessages + messageBytes
                if (accumulated.size > maxReceiveBufferSize) {
                    messages.remove(characteristic.uuid)
                    handleExceededMaxBufferSize(accumulated.size)
                } else {
                    messages[characteristic.uuid] = accumulated
                    logger.debug(
                        logTag,
                        "Chunked 'Server2Client' characteristic update: " +
                            messageBytes.toHexString()
                    )
                }

                // don't emit a message event until the message is complete.
                null
            }

            LAST_PART -> {
                val fullMessage = previousMessages + messageBytes
                messages.remove(characteristic.uuid)

                if (fullMessage.size > maxReceiveBufferSize) {
                    handleExceededMaxBufferSize(fullMessage.size)
                    return null
                }

                GattClientEvent.Message(uuid = characteristic.uuid, value = fullMessage).also {
                    logger.debug(
                        logTag,
                        "Completed 'Server2Client' message transfer:"
                    )
                    it.value.toHexString()
                        .chunked(THREE_KILOBYTE_CHAR_LENGTH)
                        .forEach { chunkedMessage ->
                            logger.debug(
                                logTag,
                                chunkedMessage
                            )
                        }
                }
            }

            else -> {
                GattClientEvent.Error(ClientError.INVALID_MESSAGE_PREFIX).also {
                    logger.debug(
                        logTag,
                        "Received invalid status byte: ${firstByte.toHexString()}"
                    )
                    messages[characteristic.uuid] = byteArrayOf()
                }
            }
        }
    }

    /**
     * Handles when the accumulated BLE buffer exceeds the configured maximum size.
     * Sends the session end command to the holder, then closes the connection and emits
     * an error to trigger session failure and destruction.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun handleExceededMaxBufferSize(bufferSize: Int) {
        logger.error(
            logTag,
            "ExceededMaxBufferSize: $bufferSize bytes exceeds limit of $maxReceiveBufferSize bytes"
        )
        isTerminating = true
        coroutineScope.launch {
            notifySessionEnd()
            delay(BLE_SEND_NOTIFICATION_DELAY.milliseconds)
            disconnect()
            _events.tryEmit(GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE))
        }
    }

    companion object {

        /** Fallback (ms) for the START write confirmation so a missing callback cannot stall setup. */
        const val START_CONFIRMATION_TIMEOUT: Long = 200L

        /**
         * The [String] length for 3 kilobytes of data, as kotlin uses 16 bits per [Char].
         * This is used for chunking long log messages due to android limitations of
         */
        const val THREE_KILOBYTE_CHAR_LENGTH = 192

        /**
         * Default maximum receive buffer size for the verifier role (2MB).
         * The verifier receives a DeviceResponse which may include an ISO 18013-5 portrait
         * image (up to ~1MB). With CBOR encoding, MSO, issuer signatures, and AES-GCM
         * encryption overhead, the total payload can reach ~1.5MB.
         */
        const val DEFAULT_MAX_RECEIVE_BUFFER_SIZE: Int = 2 * 1024 * 1024
    }
}
