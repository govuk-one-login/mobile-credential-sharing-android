package uk.gov.onelogin.sharing.bluetooth.peripheral.gattcallbacks

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import io.mockk.every
import io.mockk.mockk
import uk.gov.onelogin.sharing.bluetooth.internal.mdoc.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState

/**
 * A collection of test stubs (fixtures) for testing the GattServerCallback.
 *
 */
object CharacteristicWriteRequestStub {
    /**
     * Holds the parameters for the `onCharacteristicWriteRequest` callback.
     *
     * @param device The mocked client [android.bluetooth.BluetoothDevice] making the request.
     * @param requestId A sample request ID.
     * @param characteristic The mocked [android.bluetooth.BluetoothGattCharacteristic] being written to.
     * @param preparedWrite Simulates whether this is part of a prepared write sequence.
     * @param responseNeeded Simulates whether the client expects a response.
     * @param offset The data offset for the write operation.
     * @param value The raw byte array data being written.
     */

    data class OnCharacteristicWriteRequestArgs(
        val device: BluetoothDevice? = mockk(relaxed = true),
        val requestId: Int = 1,
        val characteristic: BluetoothGattCharacteristic? = mockk {
            every { uuid } returns GattUuids.STATE_UUID
        },
        val preparedWrite: Boolean = false,
        val responseNeeded: Boolean = true,
        val offset: Int = 0,
        val value: ByteArray?
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with the `START` command.
     */

    fun writeRequestStart() = OnCharacteristicWriteRequestArgs(
        value = byteArrayOf(MdocState.START.code)
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with an unknown command.
     */

    fun writeRequestUnknown() = OnCharacteristicWriteRequestArgs(
        value = byteArrayOf(0xFF.toByte())
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with a null value.
     */

    fun writeRequestNullValue() = OnCharacteristicWriteRequestArgs(
        value = null
    )
}
