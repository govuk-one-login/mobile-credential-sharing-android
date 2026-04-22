package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_0
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_1
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over18Request
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over21Request
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest.ItemsRequestDto

class ItemsRequestEncoderTest {

    private val cborMapper = ObjectMapper(CBORFactory()).apply { registerKotlinModule() }

    private fun decodeInnerItemsRequest(tag24Bytes: ByteArray): ItemsRequestDto {
        val parser = cborMapper.createParser(tag24Bytes)
        parser.nextToken()
        val innerBytes = (cborMapper.readTree<JsonNode>(parser) as BinaryNode).binaryValue()
        return cborMapper.readValue(innerBytes, ItemsRequestDto::class.java)
    }

    @Test
    fun `encodeCbor output starts with CBOR Tag 24 marker`() {
        val result = over21Request.encodeCbor()

        assertEquals(CBOR_TAG_24_BYTE_0.toByte(), result[0])
        assertEquals(CBOR_TAG_24_BYTE_1.toByte(), result[1])
    }

    @Test
    fun `encodeCbor inner bytes decode back to original docType`() {
        val decoded = decodeInnerItemsRequest(over21Request.encodeCbor())

        assertEquals(MDL_DOC_TYPE, decoded.docType)
    }

    @Test
    fun `encodeCbor inner bytes decode back to original nameSpaces`() {
        val decoded = decodeInnerItemsRequest(over21Request.encodeCbor())

        assertEquals(over21Request.nameSpaces[MDL_NAMESPACE], decoded.nameSpaces[MDL_NAMESPACE])
    }

    @Test
    fun `maps portrait and age_over_21 with intentToRetain false when encodeCbor invoked`() {
        val decoded = decodeInnerItemsRequest(over21Request.encodeCbor())
        val elements = decoded.nameSpaces[MDL_NAMESPACE]!!

        assertEquals(false, elements["portrait"])
        assertEquals(false, elements["age_over_21"])
        assertFalse(elements.containsKey("given_name"))
        assertFalse(elements.containsKey("family_name"))
        assertEquals(2, elements.size)
    }

    @Test
    fun `maps given_name and family_name retain true and age_over_18 false when encodeCbor`() {
        val decoded = decodeInnerItemsRequest(over18Request.encodeCbor())
        val elements = decoded.nameSpaces[MDL_NAMESPACE]!!

        assertEquals(true, elements["given_name"])
        assertEquals(true, elements["family_name"])
        assertEquals(false, elements["age_over_18"])
        assertFalse(elements.containsKey("portrait"))
        assertEquals(3, elements.size)
    }

    @Test
    fun `encodeCbor preserves namespace key`() {
        val decoded = decodeInnerItemsRequest(over21Request.encodeCbor())

        assertTrue(decoded.nameSpaces.containsKey(MDL_NAMESPACE))
    }
}
