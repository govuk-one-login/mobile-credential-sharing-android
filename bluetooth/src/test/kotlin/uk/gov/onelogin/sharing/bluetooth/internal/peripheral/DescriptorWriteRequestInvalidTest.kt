package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattDescriptor
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.FakeGattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallbackEvent

@RunWith(Parameterized::class)
class DescriptorWriteRequestInvalidTest(
    private val device: BluetoothDevice?,
    private val descriptor: BluetoothGattDescriptor?,
    private val value: ByteArray?,
    private val expectedReason: GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason
) {

    @Test
    fun `emits Invalid event when required fields are null`() {
        val emitter = FakeGattEventEmitter()
        val callback = GattServerCallback(emitter, logger = SystemLogger())

        callback.onDescriptorWriteRequest(
            device = device,
            requestId = 1,
            descriptor = descriptor,
            preparedWrite = false,
            responseNeeded = true,
            offset = 0,
            value = value
        )

        assertEquals(
            GattServerCallbackEvent.DescriptorWriteRequest.Invalid(
                requestId = 1,
                responseNeeded = true,
                reason = expectedReason
            ),
            emitter.events.firstOrNull()
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}")
        fun data(): Collection<Array<Any?>> = listOf(
            arrayOf(
                null,
                mockk<BluetoothGattDescriptor>(),
                byteArrayOf(0x01),
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.NullDevice
            ),
            arrayOf(
                mockk<BluetoothDevice>(),
                null,
                byteArrayOf(0x01),
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.NullDescriptor
            ),
            arrayOf(
                mockk<BluetoothDevice>(),
                mockk<BluetoothGattDescriptor>(),
                null,
                GattServerCallbackEvent.DescriptorWriteRequest.Invalid.Reason.EmptyValue
            )
        )
    }
}
