package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.STATE_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues.MIN_MTU
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ValidationResult
import uk.gov.onelogin.sharing.core.logger.logTag

const val INVALID_SERVICE = "Gatt Service does not have a state characteristic"

@ContributesBinding(AppScope::class)
@Suppress("TooManyFunctions")
class AndroidGattClientManager(
    private val context: Context,
    private val permissionChecker: BluetoothPermissionChecker,
    private val serviceValidator: ServiceValidator,
    private val gattWriter: GattWriter,
    private val logger: Logger
) : GattClientManager {
    private val _events = MutableSharedFlow<GattClientEvent>(
        extraBufferCapacity = 32
    )
    override val events: SharedFlow<GattClientEvent> = _events

    private var bluetoothGatt: BluetoothGatt? = null
    private var serviceUuid: UUID? = null
    private val eventEmitter = GattClientEventEmitter {
        handleGattEvent(it)
    }
    private var mtu = MIN_MTU
    private var isSessionEnd = false
    private val pendingDescriptorWrites = ArrayDeque<BluetoothGattDescriptor>()

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        if (!permissionChecker.hasBluetoothPermissions()) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                )
            )
            return
        }

        this.serviceUuid = serviceUuid
        pendingDescriptorWrites.clear()
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
            bluetoothGatt.let { bluetoothGatt } ?: return SessionEndStates.WRITE_TO_SERVER_FAILED

        val state = gatt
            .getService(serviceUuid)
            .getCharacteristic(GattUuids.STATE_UUID) ?: return handleError(
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
            }
        } catch (e: SecurityException) {
            logger.error(logTag, "Security exception", e)
            _events.tryEmit(
                GattClientEvent.Error(ClientError.BLUETOOTH_PERMISSION_MISSING)
            )
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
        val writeSuccess = gattWriter.writeCharacteristic(
            gatt = gatt,
            characteristic = state,
            value = startValue
        )

        if (writeSuccess) {
            logger.debug(logTag, "Connection state = STARTED")
            _events.tryEmit(GattClientEvent.ConnectionStateStarted)
        } else {
            handleError(
                ClientError.FAILED_TO_START,
                "Failed to write 'Start' state"
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun characteristicWritten(event: GattEvent.CharacteristicWrite) {
        if (event.status != BluetoothGatt.GATT_SUCCESS) {
            return handleError(
                ClientError.FAILED_TO_START,
                "Failed to write 'Start' state"
            )
        }

        logger.debug(logTag, "Wrote value to characteristic: ${event.characteristic.uuid}")
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
        event.value?.firstOrNull() ?: return

        if (event.characteristic.uuid == STATE_UUID) {
            when (event.value.first()) {
                MdocState.END.code -> {
                    logger.debug(logTag, "GATT: Received notification 0x02 on State")
                    isSessionEnd = true
                    bluetoothGatt?.disconnect()
                    _events.tryEmit(GattClientEvent.SessionEnd(SessionEndStates.SUCCESS))
                }
            }
        }
    }
}
