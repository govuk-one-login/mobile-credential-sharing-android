package uk.gov.onelogin.sharing.bluetooth.internal.core

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger

class MessageReassemblerTest {
    private val logger = SystemLogger()
    private val uuid: UUID = UUID.randomUUID()

    private fun reassembler(maxReceiveBufferSize: Int = 1024) =
        MessageReassembler(maxReceiveBufferSize, logger)

    @Test
    fun `single LAST_PART chunk completes immediately`() {
        val reassembler = reassembler()

        val result = reassembler.accept(
            uuid,
            byteArrayOf(LAST_PART) + byteArrayOf(0x01, 0x02, 0x03)
        )

        assertTrue(result is MessageReassembler.Result.Complete)
        assertTrue(result.message.contentEquals(byteArrayOf(0x01, 0x02, 0x03)))
    }

    @Test
    fun `NON_LAST_PART chunk is incomplete then completed by LAST_PART`() {
        val reassembler = reassembler()

        val first = reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x01, 0x02))
        assertEquals(MessageReassembler.Result.Incomplete, first)

        val second = reassembler.accept(uuid, byteArrayOf(LAST_PART) + byteArrayOf(0x03, 0x04))

        assertTrue(second is MessageReassembler.Result.Complete)
        assertTrue(second.message.contentEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04)))
    }

    @Test
    fun `accumulated NON_LAST_PART over the limit reports overflow and discards buffer`() {
        val reassembler = reassembler(maxReceiveBufferSize = 3)

        val result = reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + ByteArray(4) { 0x01 })

        assertTrue(result is MessageReassembler.Result.ExceededMaxBuffer)
        assertEquals(4, result.bufferSize)

        // Buffer was discarded: a subsequent LAST_PART only contains its own payload.
        val next = reassembler.accept(uuid, byteArrayOf(LAST_PART) + byteArrayOf(0x09))
        assertTrue(next is MessageReassembler.Result.Complete)
        assertTrue(next.message.contentEquals(byteArrayOf(0x09)))
    }

    @Test
    fun `single LAST_PART over the limit reports overflow`() {
        val reassembler = reassembler(maxReceiveBufferSize = 2)

        val result = reassembler.accept(uuid, byteArrayOf(LAST_PART) + ByteArray(3) { 0x01 })

        assertTrue(result is MessageReassembler.Result.ExceededMaxBuffer)
        assertEquals(3, result.bufferSize)
    }

    @Test
    fun `message exactly at the limit is accepted`() {
        val reassembler = reassembler(maxReceiveBufferSize = 4)

        reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + ByteArray(2) { 0x01 })
        val result = reassembler.accept(uuid, byteArrayOf(LAST_PART) + ByteArray(2) { 0x02 })

        assertTrue(result is MessageReassembler.Result.Complete)
        assertEquals(4, result.message.size)
    }

    @Test
    fun `unrecognised header byte reports invalid prefix`() {
        val reassembler = reassembler()

        val result = reassembler.accept(uuid, byteArrayOf(0x7F, 0x01))

        assertEquals(MessageReassembler.Result.InvalidPrefix, result)
    }

    @Test
    fun `reset discards a partially accumulated buffer`() {
        val reassembler = reassembler()

        reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x01, 0x02))
        reassembler.reset(uuid)

        val result = reassembler.accept(uuid, byteArrayOf(LAST_PART) + byteArrayOf(0x03))
        assertTrue(result is MessageReassembler.Result.Complete)
        assertTrue(result.message.contentEquals(byteArrayOf(0x03)))
    }

    @Test
    fun `clear discards all accumulated buffers`() {
        val reassembler = reassembler()
        val otherUuid = UUID.randomUUID()

        reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x01))
        reassembler.accept(otherUuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x02))
        reassembler.clear()

        val a = reassembler.accept(uuid, byteArrayOf(LAST_PART) + byteArrayOf(0x0A))
        val b = reassembler.accept(otherUuid, byteArrayOf(LAST_PART) + byteArrayOf(0x0B))

        assertTrue(a is MessageReassembler.Result.Complete)
        assertTrue(a.message.contentEquals(byteArrayOf(0x0A)))
        assertTrue(b is MessageReassembler.Result.Complete)
        assertTrue(b.message.contentEquals(byteArrayOf(0x0B)))
    }

    @Test
    fun `buffers for different UUIDs are kept independent`() {
        val reassembler = reassembler()
        val otherUuid = UUID.randomUUID()

        reassembler.accept(uuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x01))
        reassembler.accept(otherUuid, byteArrayOf(NON_LAST_PART) + byteArrayOf(0x02))

        val a = reassembler.accept(uuid, byteArrayOf(LAST_PART) + byteArrayOf(0x11))
        val b = reassembler.accept(otherUuid, byteArrayOf(LAST_PART) + byteArrayOf(0x22))

        assertTrue(a is MessageReassembler.Result.Complete)
        assertTrue(a.message.contentEquals(byteArrayOf(0x01, 0x11)))
        assertTrue(b is MessageReassembler.Result.Complete)
        assertTrue(b.message.contentEquals(byteArrayOf(0x02, 0x22)))
    }
}
