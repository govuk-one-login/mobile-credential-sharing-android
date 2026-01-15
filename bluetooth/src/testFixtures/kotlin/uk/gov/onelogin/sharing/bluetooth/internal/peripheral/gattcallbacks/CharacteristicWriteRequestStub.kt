package uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
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
        val device: BluetoothDevice = mockk(relaxed = true),
        val requestId: Int = 1,
        val characteristic: BluetoothGattCharacteristic = mockk {
            every { uuid } returns UUID.randomUUID()
        },
        val preparedWrite: Boolean = false,
        val responseNeeded: Boolean = true,
        val offset: Int = 0,
        val value: ByteArray
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with the `START` command.
     */

    fun writeRequestStart() = OnCharacteristicWriteRequestArgs(
        characteristic = mockk {
            every { uuid } returns GattUuids.STATE_UUID
        },
        value = byteArrayOf(MdocState.START.code)
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with an unknown command.
     */

    fun writeRequestUnknown() = OnCharacteristicWriteRequestArgs(
        value = byteArrayOf(0xFF.toByte())
    )

    /**
     * Creates a stub for `onCharacteristicWriteRequest` with an empty value.
     */

    fun writeRequestEmptyValue() = OnCharacteristicWriteRequestArgs(
        value = byteArrayOf()
    )

    /**
     * Creates a stub for `onChararacteristicWriteRequest` with message segment
     * received from remote device
     */
    fun writeRequestMessage(message: ByteArray) = OnCharacteristicWriteRequestArgs(
        characteristic = mockk {
            every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
        },
        value = message
    )
}
