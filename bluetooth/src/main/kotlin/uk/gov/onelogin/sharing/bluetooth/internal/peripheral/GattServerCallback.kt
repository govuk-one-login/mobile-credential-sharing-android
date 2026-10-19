package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.core.MessageReassembler
import uk.gov.onelogin.sharing.core.logger.LoggerException
import uk.gov.onelogin.sharing.core.logger.logTag

internal class GattServerCallback(
    private val gatGattEventEmitter: GattServerEventEmitter,
    private val logger: Logger,
    private val reassembler: MessageReassembler
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

        when (characteristic.uuid) {
            GattUuids.STATE_UUID -> {
                handleStateUpdate(device, value)
            }

            else -> {
                when (val result = reassembler.accept(characteristic.uuid, value)) {
                    is MessageReassembler.Result.Incomplete -> {
                        logger.debug(logTag, "received message part, expecting additional data")
                    }

                    is MessageReassembler.Result.ExceededMaxBuffer -> {
                        emitExceededMaxBufferSize()
                    }

                    is MessageReassembler.Result.Complete -> {
                        gatGattEventEmitter.emit(
                            GattServerCallbackEvent.MessageReceived(device, result.message)
                        )

                        val fullMessageString = result.message
                            .joinToString { BYTE_TO_HEX_FORMAT.format(it) }

                        logger.debug(logTag, "received message: $fullMessageString")
                    }

                    is MessageReassembler.Result.InvalidPrefix -> {
                        val message = "invalid message received from remote device"
                        logger.error(logTag, message, LoggerException(message))
                    }
                }
            }
        }
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

    private fun handleStateUpdate(device: BluetoothDevice, value: ByteArray) {
        val state = value.firstOrNull()?.let {
            MdocState.fromByte(it)
        }

        when (state) {
            MdocState.START -> {
                logger.debug(logTag, "Received START command from ${device.address}")
                gatGattEventEmitter.emit(GattServerCallbackEvent.ConnectionStateStarted)
            }

            MdocState.END -> {
                logger.debug(logTag, "GATT: Received Write Request 0x02 on State")
                gatGattEventEmitter.emit(GattServerCallbackEvent.SessionEnd)
            }

            null -> {
                logger.debug(
                    logTag,
                    "Unknown or empty command: ${
                        value.joinToString {
                            BYTE_TO_HEX_FORMAT.format(it)
                        }
                    }"
                )
            }
        }
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

    private fun emitExceededMaxBufferSize() {
        gatGattEventEmitter.emit(GattServerCallbackEvent.ExceededMaxBufferSize)
    }

    companion object {
        private const val BYTE_TO_HEX_FORMAT = "%02X"

        const val DEFAULT_MAX_RECEIVE_BUFFER_SIZE: Int = 64 * 1024
    }
}
