package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

internal class GattServerCallback(
    private val gatGattEventEmitter: GattEventEmitter,
    private val logger: Logger
) : BluetoothGattServerCallback() {
    override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
        logger.debug(logTag, "Address: ${device.address}")
        logger.debug(logTag, "Status: $status")
        logger.debug(logTag, "NewState: $newState")
        gatGattEventEmitter.emit(
            GattEvent.ConnectionStateChange(
                status,
                newState,
                device
            )
        )
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        logger.debug(logTag, "onCharacteristicWriteRequest")
        val logs = listOf(
            "Device: ${device?.address}",
            "RequestId: $requestId",
            "Characteristic UUID: ${characteristic?.uuid}",
            "PreparedWrite: $preparedWrite",
            "ResponseNeeded: $responseNeeded",
            "Offset: $offset"
        )

        logger.debug(logTag, logs.joinToString(separator = "\n"))

        val state = value?.firstOrNull()?.let {
            MdocState.fromByte(it)
        }

        when (state) {
            MdocState.START -> {
                logger.debug(logTag, "Received START command from ${device?.address}")
                gatGattEventEmitter.emit(GattEvent.ConnectionStateStarted)
            }

            null -> {
                logger.debug(
                    logTag,
                    "Unknown or empty command: ${
                        value?.joinToString {
                            BYTE_TO_HEX_FORMAT.format(it)
                        }
                    }"
                )
            }
        }
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        gatGattEventEmitter.emit(
            GattEvent.ServiceAdded(status, service)
        )
    }

    companion object {
        private const val BYTE_TO_HEX_FORMAT = "%02X"
    }
}
