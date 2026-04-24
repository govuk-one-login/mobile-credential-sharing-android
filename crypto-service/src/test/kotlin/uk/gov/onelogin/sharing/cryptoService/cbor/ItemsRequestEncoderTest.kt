package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over18Request
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over21Request
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest.ItemsRequestDto

class ItemsRequestEncoderTest {

    private val cborMapper = ObjectMapper(CBORFactory()).apply { registerKotlinModule() }

    private fun decode(bytes: ByteArray): ItemsRequestDto =
        cborMapper.readValue(bytes, ItemsRequestDto::class.java)

    @Test
    fun `encodeCbor decodes back to original docType`() {
        assertEquals(
            MDL_DOC_TYPE,
            decode(over21Request.encodeCbor()).docType
        )
    }

    @Test
    fun `encodeCbor decodes back to original nameSpaces`() {
        assertEquals(
            over21Request.nameSpaces[MDL_NAMESPACE],
            decode(over21Request.encodeCbor()).nameSpaces[MDL_NAMESPACE]
        )
    }

    @Test
    fun `encodeCbor maps portrait and age_over_21 with intentToRetain false`() {
        val elements =
            decode(over21Request.encodeCbor()).nameSpaces[MDL_NAMESPACE]!!

        assertEquals(false, elements["portrait"])
        assertEquals(false, elements["age_over_21"])
        assertFalse(elements.containsKey("given_name"))
        assertFalse(elements.containsKey("family_name"))
        assertEquals(2, elements.size)
    }

    @Test
    fun `maps given_name and family_name retain true and age_over_18 false`() {
        val elements =
            decode(over18Request.encodeCbor()).nameSpaces[MDL_NAMESPACE]!!

        assertEquals(true, elements["given_name"])
        assertEquals(true, elements["family_name"])
        assertEquals(false, elements["age_over_18"])
        assertFalse(elements.containsKey("portrait"))
        assertEquals(3, elements.size)
    }

    @Test
    fun `encodeCbor preserves namespace key`() {
        assertTrue(
            decode(over21Request.encodeCbor()).nameSpaces.containsKey(
                MDL_NAMESPACE
            )
        )
    }
}
