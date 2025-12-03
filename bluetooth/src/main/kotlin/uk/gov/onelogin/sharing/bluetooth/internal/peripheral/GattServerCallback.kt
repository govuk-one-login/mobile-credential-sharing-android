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
) :
    BluetoothGattServerCallback() {
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
        println("onCharacteristicWriteRequest")
        println("Device: ${device?.address}")
        println("RequestId: $requestId")
        println("Characteristic UUID: ${characteristic?.uuid}")
        println("PreparedWrite: $preparedWrite")
        println("ResponseNeeded: $responseNeeded")
        println("Offset: $offset")

        val state = value?.firstOrNull()?.let {
            MdocState.fromByte(it)
        }

        when (state) {
            MdocState.START -> {
                println("Received START command from ${device?.address}")
                gatGattEventEmitter.emit(GattEvent.ConnectionStateStarted)
            }

            null -> {
                println(
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
