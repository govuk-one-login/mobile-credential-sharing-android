package uk.gov.onelogin.sharing.models.mdoc.cbor

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import uk.gov.onelogin.sharing.models.EmbeddedCborStub.EXPECTED_BYTES
import uk.gov.onelogin.sharing.models.MdocStubStrings.UUID

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
        val uuid = UUID.toByteArray()
        val embeddedCbor = EmbeddedCbor(uuid)
        assertEquals(UUID, embeddedCbor.encoded.decodeToString())
    }

    @Test
    fun `serializer prefixes data with CBOR Tag 24`() {
        val cborMapper = testMapper()

        val testCborObject = EmbeddedCbor(UUID.toByteArray())
        val cborBytes = cborMapper.writeValueAsBytes(testCborObject)

        assertArrayEquals(EXPECTED_BYTES, cborBytes)
    }
}
