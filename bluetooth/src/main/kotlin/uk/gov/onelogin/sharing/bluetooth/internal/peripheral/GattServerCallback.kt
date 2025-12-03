package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
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

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        gatGattEventEmitter.emit(
            GattEvent.ServiceAdded(status, service)
        )
    }
}
