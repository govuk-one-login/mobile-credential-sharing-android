package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import org.junit.Assert.assertArrayEquals
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

class ReaderAuthenticationDtoTest {

    private val sessionTranscript = byteArrayOf(0x83.toByte(), 0x01, 0x02, 0xF6.toByte())
    private val itemsRequestBytes = byteArrayOf(0xD8.toByte(), 0x18, 0x43, 0x01, 0x02, 0x03)

    private val dto = ReaderAuthenticationDto(
        sessionTranscript = sessionTranscript,
        itemsRequestBytes = itemsRequestBytes
    )

    @Test
    fun `equals and hashCode work correctly`() {
        val same = ReaderAuthenticationDto(sessionTranscript, itemsRequestBytes)
        val differentTranscript = dto.copy(sessionTranscript = byteArrayOf(0x01))
        val differentItems = dto.copy(itemsRequestBytes = byteArrayOf(0x02))

        assertEquals(dto, same)
        assertEquals(dto.hashCode(), same.hashCode())

        assertNotEquals(dto, differentTranscript)
        assertNotEquals(dto, differentItems)
    }

    @Test
    fun `serializes to correct CBOR array structure`() {
        val bytes = dto.toCbor()
        val node = CborMapper.default.readTree(bytes)

        assertTrue(node.isArray)
        assertEquals(3, node.size())
        assertEquals("ReaderAuthentication", node.get(0).asText())
        assertEquals("ReaderAuthentication", node.get(0).asText())

        val manualBytes = CborMapper.default.writeValueAsBytes(dto)
        assertArrayEquals(bytes, manualBytes)
    }
}
