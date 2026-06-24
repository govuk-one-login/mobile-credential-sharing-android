package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.CBOR_TAG_24_BYTE_0
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.CBOR_TAG_24_BYTE_1
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.deviceRequestStub

class DeviceRequestDtoTest {

    private val dto = deviceRequestStub
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
    fun `DeviceRequestDto serializer writes text key version`() {
        val parser = CBORFactory().createParser(encoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        assertEquals("version", parser.currentName())
    }

    @Test
    fun `DeviceRequestDto serializer writes text key docRequests`() {
        val parser = CBORFactory().createParser(encoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        assertEquals("docRequests", parser.currentName())
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
    fun `DocRequestDto serializer writes text key itemsRequest`() {
        val docEncoded = dto.docRequest.first().toCbor()
        val parser = CBORFactory().createParser(docEncoded).apply { codec = CborMapper.default }
        parser.nextToken()
        parser.nextToken()
        assertEquals("itemsRequest", parser.currentName())
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
