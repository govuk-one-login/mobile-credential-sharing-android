package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import uk.gov.logging.api.v3.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

internal class GattServerCallback(
    private val gatGattEventEmitter: GattServerEventEmitter,
    private val logger: Logger
) : BluetoothGattServerCallback() {
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        logger.debug(logTag, "Address: ${device.address}")
        logger.debug(logTag, "Status: $status")
        logger.debug(logTag, "NewState: $newState")
        gatGattEventEmitter.emit(
            GattServerCallbackEvent.ConnectionStateChange(
                status,
                newState,
                device
            )
        )
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        logger.debug(logTag, "onCharacteristicWriteRequest")
        val logs = listOf(
            "Device: ${device.address}",
            "RequestId: $requestId",
            "Characteristic UUID: ${characteristic.uuid}",
            "PreparedWrite: $preparedWrite",
            "ResponseNeeded: $responseNeeded",
            "Offset: $offset"
        )
        logger.debug(logTag, logs.joinToString(separator = "\n"))

        gatGattEventEmitter.emit(
            GattServerCallbackEvent.CharacteristicWriteRequest(
                device = device,
                characteristicUuid = characteristic.uuid,
                value = value
            )
        )
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        val event: GattServerCallbackEvent.DescriptorWriteRequest = when {
            device == null -> GattServerCallbackEvent.DescriptorWriteRequest.Invalid(
                requestId,
                responseNeeded,
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.NullDevice
            )

            descriptor == null -> GattServerCallbackEvent.DescriptorWriteRequest.Invalid(
                requestId,
                responseNeeded,
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.NullDescriptor
            )

            value == null -> GattServerCallbackEvent.DescriptorWriteRequest.Invalid(
                requestId,
                responseNeeded,
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.EmptyValue
            )

            else -> GattServerCallbackEvent.DescriptorWriteRequest.Valid(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }

        gatGattEventEmitter.emit(event)
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService) {
        gatGattEventEmitter.emit(
            GattServerCallbackEvent.ServiceAdded(status, service)
        )
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        gatGattEventEmitter.emit(
            GattServerCallbackEvent.MtuChanged(device, mtu)
        )
    }
}
