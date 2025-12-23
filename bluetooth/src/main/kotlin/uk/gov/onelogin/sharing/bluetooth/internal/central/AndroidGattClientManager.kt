package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import androidx.annotation.RequiresPermission
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientManager
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.core.logger.logTag

internal class AndroidGattClientManager(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
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

    private fun discoverServices() {
        try {
            bluetoothGatt?.discoverServices()
        } catch (e: SecurityException) {
            logger.error(logTag, "Security exception", e)
            _events.tryEmit(
                GattClientEvent.Error(ClientError.BLUETOOTH_PERMISSION_MISSING)
            )
        }
    }

    private fun handleGattEvent(event: GattEvent) {
        when (event) {
            is GattEvent.ConnectionStateChange -> {
                val address = event.gatt.device.address

                val clientEvent = when {
                    event.status == BluetoothGatt.GATT_SUCCESS &&
                        event.newState == BluetoothGatt.STATE_CONNECTED -> {
                        bluetoothGatt = event.gatt

                        discoverServices()

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

                val service = event.bluetoothGatt.getService(serviceUuid)
                if (service == null) {
                    _events.tryEmit(
                        GattClientEvent.Error(
                            ClientError.SERVICE_NOT_FOUND
                        )
                    )
                    return
                }

                _events.tryEmit(
                    GattClientEvent.ServicesDiscovered(service)
                )
            }
        }
    }
}
