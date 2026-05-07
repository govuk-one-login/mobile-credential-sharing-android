package uk.gov.onelogin.sharing.bluetooth.api.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import java.util.UUID
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.core.logger.logTag

class GattServerCallback(
    private val gatGattEventEmitter: GattEventEmitter,
    private val logger: Logger,
    private var messages: MutableMap<UUID, ByteArray> = mutableMapOf()
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
                // join messages until 0x00
                val previousMessages = messages[characteristic.uuid] ?: byteArrayOf()

                val indicator = value.firstOrNull()

                val message = value.drop(1)
                val newMessage = ByteArray(message.count()) { message[it] }

                when (indicator) {
                    NON_LAST_PART -> {
                        messages[characteristic.uuid] = previousMessages + newMessage
                        logger.debug(logTag, "received message part, expecting additional data")
                    }

                    LAST_PART -> {
                        messages.remove(characteristic.uuid)

                        val fullMessage = previousMessages + newMessage

                        gatGattEventEmitter.emit(
                            GattServerCallbackEvent.MessageReceived(fullMessage)
                        )

                        val fullMessageString = fullMessage
                            .joinToString { BYTE_TO_HEX_FORMAT.format(it) }

                        logger.debug(logTag, "received message: $fullMessageString")
                    }

                    else -> {
                        logger.error(logTag, "invalid message received from remote device")
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

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        gatGattEventEmitter.emit(
            GattServerCallbackEvent.ServiceAdded(status, service)
        )
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        gatGattEventEmitter.emit(
            GattServerCallbackEvent.MtuChanged(device, mtu)
        )
    }

    companion object {
        private const val BYTE_TO_HEX_FORMAT = "%02X"

        const val NON_LAST_PART: Byte = 0x01
        const val LAST_PART: Byte = 0x00
    }
}
