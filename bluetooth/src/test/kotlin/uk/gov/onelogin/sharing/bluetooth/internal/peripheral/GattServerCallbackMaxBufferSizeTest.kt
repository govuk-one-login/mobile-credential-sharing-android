package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.FakeGattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallbackEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestMessage

class GattServerCallbackMaxBufferSizeTest {
    private val fakeEmitter = FakeGattEventEmitter()
    private val device = mockk<BluetoothDevice>()
    private val characteristicUuid = GattUuids.CLIENT_2_SERVER_UUID
    private val characteristic = mockk<android.bluetooth.BluetoothGattCharacteristic> {
        every { uuid } returns characteristicUuid
    }

    @Before
    fun setup() {
        fakeEmitter.events.clear()
        every { device.address } returns DEVICE_ADDRESS
    }

    @Test
    fun `multi-part message within 64KB limit emits MessageReceived`() {
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = GattServerCallback.DEFAULT_MAX_RECEIVE_BUFFER_SIZE
        )

        val chunk1 = byteArrayOf(GattServerCallback.NON_LAST_PART) + ByteArray(100) { 0x11 }
        sendChunk(callback, chunk1)
        assertEquals(0, fakeEmitter.events.size)

        val chunk2 = byteArrayOf(GattServerCallback.LAST_PART) + ByteArray(50) { 0x22 }
        sendChunk(callback, chunk2)

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.MessageReceived
        assertEquals(150, event.byteArray.size)
    }

    @Test
    fun `single-part message within limit emits MessageReceived`() {
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = GattServerCallback.DEFAULT_MAX_RECEIVE_BUFFER_SIZE
        )

        val message = byteArrayOf(GattServerCallback.LAST_PART) + ByteArray(200) { 0x33 }
        sendChunk(callback, message)

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.MessageReceived
        assertEquals(200, event.byteArray.size)
    }

    @Test
    fun `chunk exceeding 64KB limit emits ExceededMaxBufferSize`() {
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = GattServerCallback.DEFAULT_MAX_RECEIVE_BUFFER_SIZE
        )

        val almostFull =
            byteArrayOf(GattServerCallback.NON_LAST_PART) + ByteArray(64 * 1024) { 0x11 }
        sendChunk(callback, almostFull)
        assertEquals(0, fakeEmitter.events.size)

        val overflow = byteArrayOf(GattServerCallback.NON_LAST_PART, 0x01)
        sendChunk(callback, overflow)

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattServerCallbackEvent.ExceededMaxBufferSize,
            fakeEmitter.events.single()
        )
    }

    @Test
    fun `single LAST_PART chunk exceeding limit emits ExceededMaxBufferSize`() {
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = 10
        )

        val oversized = byteArrayOf(GattServerCallback.LAST_PART) + ByteArray(11) { 0x11 }
        sendChunk(callback, oversized)

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattServerCallbackEvent.ExceededMaxBufferSize,
            fakeEmitter.events.single()
        )
    }

    @Test
    fun `buffer is removed after exceeding limit`() {
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = 100
        )

        val chunk = byteArrayOf(GattServerCallback.NON_LAST_PART) + ByteArray(101) { 0x11 }
        sendChunk(callback, chunk)

        assertEquals(
            GattServerCallbackEvent.ExceededMaxBufferSize,
            fakeEmitter.events.single()
        )

        // Clear emitter and send a LAST_PART
        fakeEmitter.events.clear()
        val lastPart = byteArrayOf(GattServerCallback.LAST_PART, 0x22)
        sendChunk(callback, lastPart)

        // The MessageReceived should contain only the LAST_PART payload (no accumulated buffer)
        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.MessageReceived
        assertEquals(1, event.byteArray.size)
        assertEquals(0x22.toByte(), event.byteArray[0])
    }

    @Test
    fun `message exactly at custom limit is accepted`() {
        val customLimit = 100
        val callback = GattServerCallback(
            gatGattEventEmitter = fakeEmitter,
            logger = SystemLogger(),
            maxReceiveBufferSize = customLimit
        )

        val firstPart =
            byteArrayOf(GattServerCallback.NON_LAST_PART) + ByteArray(50) { 0x11 }
        sendChunk(callback, firstPart)
        assertEquals(0, fakeEmitter.events.size)

        val lastPart =
            byteArrayOf(GattServerCallback.LAST_PART) + ByteArray(50) { 0x22 }
        sendChunk(callback, lastPart)

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.MessageReceived
        assertEquals(100, event.byteArray.size)
    }

    private fun sendChunk(callback: GattServerCallback, message: ByteArray) {
        writeRequestMessage(device, characteristic, message).run {
            callback.onCharacteristicWriteRequest(
                device,
                requestId,
                this.characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }
    }
}
