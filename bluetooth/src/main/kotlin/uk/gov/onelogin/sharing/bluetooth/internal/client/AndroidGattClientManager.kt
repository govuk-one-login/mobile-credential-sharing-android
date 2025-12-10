package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.core.logger.logTag
import java.util.UUID

internal class AndroidGattClientManager(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
    private val mdocServiceValidator: ServiceValidator,
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

    override fun connect(
        device: BluetoothDevice,
        serviceUuid: UUID
    ) {
        if (!permissionChecker.hasPermission()) {
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
                    ClientError.BLUETOOTH_GATT_NOT_AVAILABLE
                )
            )
            null
        }
    }

    override fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    override fun writeState(command: MdocState) {
        TODO("Not yet implemented")
    }

    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.ConnectionStateChange -> {

            }

            is GattEvent.ServicesDiscovered -> {
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

                val validationResult = mdocServiceValidator.validate(service)
                when (validationResult) {
                    ValidationResult.Success -> {}
                    is ValidationResult.Failure -> {}
                }
            }
        }
    }
}