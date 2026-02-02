package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState

/**
 * A collection of test stubs (fixtures) for testing the GattServerCallback.
 *
 */
object CharacteristicWriteRequestStub {
    /**
     * Holds the parameters for the `onCharacteristicWriteRequest` callback.
     *
     * @param device The mocked client [BluetoothDevice] making the request.
     * @param requestId A sample request ID.
     * @param characteristic The mocked [BluetoothGattCharacteristic] being written to.
     * @param preparedWrite Simulates whether this is part of a prepared write sequence.
     * @param responseNeeded Simulates whether the client expects a response.
     * @param offset The data offset for the write operation.
     * @param value The raw byte array data being written.
     */

    data class OnCharacteristicWriteRequestArgs(
        val device: BluetoothDevice,
        val requestId: Int = 1,
        val characteristic: BluetoothGattCharacteristic,
        val preparedWrite: Boolean = false,
        val responseNeeded: Boolean = true,
        val offset: Int = 0,
        val value: ByteArray
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with the `START` command.
     */

    fun writeRequestStart(
        bluetoothDevice: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic
    ) = OnCharacteristicWriteRequestArgs(
        device = bluetoothDevice,
        characteristic = characteristic,
        value = byteArrayOf(MdocState.START.code)
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with an unknown command.
     */

    fun writeRequestUnknown(
        bluetoothDevice: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic
    ) = OnCharacteristicWriteRequestArgs(
        device = bluetoothDevice,
        characteristic = characteristic,
        value = byteArrayOf(0xFF.toByte())
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with an empty value.
     */

    fun writeRequestEmptyValue(
        bluetoothDevice: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic
    ) = OnCharacteristicWriteRequestArgs(
        device = bluetoothDevice,
        characteristic = characteristic,
        value = byteArrayOf()
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with message segment
     * received from remote device
     */
    fun writeRequestMessage(
        bluetoothDevice: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic,
        message: ByteArray
    ) = OnCharacteristicWriteRequestArgs(
        device = bluetoothDevice,
        characteristic = characteristic,
        value = message
    )
}
