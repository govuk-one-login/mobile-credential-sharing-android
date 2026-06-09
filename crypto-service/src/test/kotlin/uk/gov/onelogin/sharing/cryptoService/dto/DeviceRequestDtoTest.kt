package uk.gov.onelogin.sharing.cryptoService.dto

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_0
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_1
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.toDto

class DeviceRequestDtoTest {

    private val dto = deviceRequestStub.toDto()
    private val encoded = dto.toCbor()

    @Test
    fun `DeviceRequestDto preserves version`() {
        val decoded = CborMapper.default.readValue(encoded, DeviceRequestDto::class.java)
        assertEquals("1.0", decoded.version)
    }

    @Test
    fun `DeviceRequestDto preserves docRequest count`() {
        val decoded = CborMapper.default.readValue(encoded, DeviceRequestDto::class.java)
        assertEquals(1, decoded.docRequest.size)
    }

    @Test
    fun `DeviceRequestDto serializer writes integer key 0 for version`() {
        val parser = CBORFactory().createParser(encoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        assertEquals(0, parser.currentName().toLong())
    }

    @Test
    fun `DeviceRequestDto serializer writes integer key 1 for docRequests`() {
        val parser = CBORFactory().createParser(encoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        assertEquals(1, parser.currentName().toLong())
    }

    @Test
    fun `DocRequestDto preserves docType`() {
        val docDto = dto.docRequest.first()
        val docEncoded = docDto.toCbor()
        val decoded = CborMapper.default.readValue(docEncoded, DocRequestDto::class.java)
        assertEquals(MDL_DOC_TYPE, decoded.itemsRequest.docType)
    }

    @Test
    fun `DocRequestDto preserves nameSpaces`() {
        val docDto = dto.docRequest.first()
        val docEncoded = docDto.toCbor()
        val decoded = CborMapper.default.readValue(docEncoded, DocRequestDto::class.java)
        assertEquals(
            mapOf("age_over_18" to false),
            decoded.itemsRequest.nameSpaces[MDL_NAMESPACE]
        )
    }

    @Test
    fun `DocRequestDto serializer encodes itemsRequest as Tag 24 bstr`() {
        val docEncoded = dto.docRequest.first().toCbor()
        val tag24Sequence = byteArrayOf(CBOR_TAG_24_BYTE_0.toByte(), CBOR_TAG_24_BYTE_1.toByte())
        assertTrue(docEncoded.toList().windowed(2).any { it == tag24Sequence.toList() })
    }

    @Test
    fun `DocRequestDto serializer writes integer key 1 for itemsRequest`() {
        assertTrue(dto.docRequest.first().toCbor().any { it == 0x01.toByte() })
    }

    @Test
    fun `ItemsRequestDto bytes inside Tag 24 decode to correct docType`() {
        val docEncoded = dto.docRequest.first().toCbor()
        val parser = CBORFactory().createParser(docEncoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val innerBytes = parser.binaryValue
        val itemsRequest = CborMapper.default.readValue(innerBytes, ItemsRequestDto::class.java)
        assertEquals(MDL_DOC_TYPE, itemsRequest.docType)
    }

    @Test
    fun `ItemsRequestDto bytes inside Tag 24 decode to correct nameSpaces`() {
        val docEncoded = dto.docRequest.first().toCbor()
        val parser = CBORFactory().createParser(docEncoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val innerBytes = parser.binaryValue
        val itemsRequest = CborMapper.default.readValue(innerBytes, ItemsRequestDto::class.java)
        assertEquals(mapOf("age_over_18" to false), itemsRequest.nameSpaces[MDL_NAMESPACE])
    }
}
