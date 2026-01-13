package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.validator.ValidationResult
import uk.gov.onelogin.sharing.core.logger.logTag

internal class AndroidGattClientManager(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
    private val serviceValidator: ServiceValidator,
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

    override fun connect(device: BluetoothDevice, serviceUuid: UUID) {
        if (!permissionChecker.hasCentralPermissions()) {
            _events.tryEmit(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                )
            )
            return
        }

        this.serviceUuid = serviceUuid
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
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private fun handleGattEvent(event: GattEvent) {
        try {
            when (event) {
                is GattEvent.ConnectionStateChange -> connectionChanged(event)
                is GattEvent.ServicesDiscovered -> servicesDiscovered(event)
                is GattEvent.MtuChange -> changedMtu(event)
                is GattEvent.CharacteristicWrite -> characteristicWritten(event)
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
                bluetoothGatt = null
                GattClientEvent.Disconnected(address)
            }

            else -> GattClientEvent.UnsupportedEvent(
                address,
                event.status,
                event.newState
            )
        }

        _events.tryEmit(clientEvent)
    }

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

        gatt.requestMtu(MtuValues.MAX_POSSIBLE)

        val state = service
            .getCharacteristic(GattUuids.STATE_UUID) ?: return handleError(
            ClientError.INVALID_SERVICE,
            "Gatt Service does not have a state characteristic"
        )

        val serverToClient = service
            .getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) ?: return handleError(
            ClientError.INVALID_SERVICE,
            "Gatt Service does not have a server to client characteristic"
        )

        // Subscribe to inbound messages
        val success = gatt.setCharacteristicNotification(
            state,
            true
        ) && gatt.setCharacteristicNotification(serverToClient, true)

        if (success) {
            logger.debug(logTag, "subscribed to bluetooth characteristic changes")
            _events.tryEmit(GattClientEvent.ServicesDiscovered)
        } else {
            handleError(
                ClientError.FAILED_TO_SUBSCRIBE,
                "Failed to subscribe to characteristics"
            )
        }
    }

    @Suppress("DEPRECATION")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun changedMtu(event: GattEvent.MtuChange) {
        val gatt = bluetoothGatt.let { bluetoothGatt } ?: return
        logger.debug(logTag, "MTU negotiated: ${event.mtu}")

        val state = gatt
            .getService(serviceUuid)
            .getCharacteristic(GattUuids.STATE_UUID) ?: return handleError(
            ClientError.INVALID_SERVICE,
            "Gatt Service does not have a state characteristic"
        )

        // Set the state value to start
        val startValue = byteArrayOf(MdocState.START.code)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                state,
                startValue,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            )
        } else {
            state.value = startValue
            gatt.writeCharacteristic(state)
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
}
