package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

class SessionDataEncoderTest {
    private val cborMapper = ObjectMapper(CBORFactory())

    private fun decodeCborMap(bytes: ByteArray): Map<String, Any> =
        cborMapper.readValue(bytes, Map::class.java) as Map<String, Any>

    @Test
    fun `encodes SessionData with data only`() {
        val payload = byteArrayOf(0x01, 0x02, 0x03)
        val sessionData = SessionData(data = payload)

        val result = decodeCborMap(sessionData.encodeCbor())

        assertTrue(result.containsKey("data"))
        assertFalse(result.containsKey("status"))
        assertArrayEquals(payload, result["data"] as ByteArray)
    }

    @Test
    fun `encodes SessionData with status only`() {
        val sessionData = SessionData(status = SessionDataStatus.SESSION_TERMINATION)

        val result = decodeCborMap(sessionData.encodeCbor())

        assertFalse(result.containsKey("data"))
        assertTrue(result.containsKey("status"))
        assertEquals(20, result["status"])
    }

    @Test
    fun `encodes SessionData with both data and status`() {
        val payload = byteArrayOf(0x0A, 0x0B, 0x0C)
        val sessionData = SessionData(
            data = payload,
            status = SessionDataStatus.SESSION_TERMINATION
        )

        val result = decodeCborMap(sessionData.encodeCbor())

        assertTrue(result.containsKey("data"))
        assertTrue(result.containsKey("status"))
        assertArrayEquals(payload, result["data"] as ByteArray)
        assertEquals(20, result["status"])
    }
}
