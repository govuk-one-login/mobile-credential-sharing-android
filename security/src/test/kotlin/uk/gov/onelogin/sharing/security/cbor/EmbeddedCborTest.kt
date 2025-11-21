package uk.gov.onelogin.sharing.security.cbor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import uk.gov.onelogin.sharing.security.BleRetrievalStub.UUID_STRING
import uk.gov.onelogin.sharing.security.EmbeddedCborStub.EXPECTED_BYTES
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer

class EmbeddedCborTest {
    private fun testMapper(): ObjectMapper {
        val module =
            SimpleModule().addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
        return CBORMapper.builder(CBORFactory())
            .addModule(module)
            .build()
    }

    @Test
    fun `encodes data to CBOR`() {
        val uuid = UUID_STRING.toByteArray()
        val embeddedCbor = EmbeddedCbor(uuid)
        assertEquals(UUID_STRING, embeddedCbor.encoded.decodeToString())
    }

    @Test
    fun `serializer prefixes data with CBOR Tag 24`() {
        val cborMapper = testMapper()

        val testCborObject = EmbeddedCbor(UUID_STRING.toByteArray())
        val cborBytes = cborMapper.writeValueAsBytes(testCborObject)

        assertArrayEquals(EXPECTED_BYTES, cborBytes)
    }
}
