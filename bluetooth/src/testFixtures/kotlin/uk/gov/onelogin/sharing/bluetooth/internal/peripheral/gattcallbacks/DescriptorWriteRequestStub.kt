package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattDescriptor

/**
 * A stub object for providing mock arguments used in GATT descriptor write requests.
 */
object DescriptorWriteRequestStub {

    /**
     * Holds parameters for the `onDescriptorWriteRequest` callback.
     *
     * @property device The remote device that requested the write.
     * @property requestId The ID of the request.
     * @property descriptor The descriptor to be written to.
     * @property offset The offset within the descriptor's value.
     * @property preparedWrite Whether this is a prepared write request.
     * @property responseNeeded Whether a response is required for this request.
     * @property value The value to be written to the descriptor.
     */
    data class OnDescriptorWriteRequestArgs(
        val device: BluetoothDevice,
        val requestId: Int = 1,
        val descriptor: BluetoothGattDescriptor,
        val offset: Int = 0,
        val preparedWrite: Boolean = false,
        val responseNeeded: Boolean = true,
        val value: ByteArray = byteArrayOf()
    )
}
