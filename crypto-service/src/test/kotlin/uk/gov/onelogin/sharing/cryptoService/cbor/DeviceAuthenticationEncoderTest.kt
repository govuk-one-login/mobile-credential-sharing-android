package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceAuthentication

class DeviceAuthenticationEncoderTest {

    private val cborMapper = ObjectMapper(CBORFactory())

    private fun encodeDeviceNameSpacesBytes(): ByteArray = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray().let { EmbeddedCbor(it).toCbor() }

    private fun DeviceAuthentication.encode(): ByteArray {
        val array = ByteArrayOutputStream().also { out ->
            out.write(0x84)
            CBORFactory().createGenerator(out).use { gen -> gen.writeString(label) }
            out.write(sessionTranscript)
            CBORFactory().createGenerator(out).use { gen -> gen.writeString(docType) }
            out.write(deviceNameSpacesBytes)
        }.toByteArray()
        return EmbeddedCbor(array).toCbor()
    }

    @Test
    fun `encodeCbor output starts with CBOR Tag 24 marker`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val deviceAuthentication = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        assertEquals(0xD8.toByte(), deviceAuthentication[0])
        assertEquals(0x18.toByte(), deviceAuthentication[1])
        assertEquals(0xD8.toByte(), deviceNameSpacesBytes[0])
        assertEquals(0x18.toByte(), deviceNameSpacesBytes[1])
    }

    @Test
    fun `encodeCbor inner array has 4 elements`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val result = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        val innerArray = decodeTag24Inner(result)
        assertEquals(DeviceAuthentication.ELEMENT_COUNT, innerArray.size())
    }

    @Test
    fun `encodeCbor first element is DeviceAuthentication label`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val result = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        val innerArray = decodeTag24Inner(result)
        assertEquals(DeviceAuthentication.DEVICE_AUTHENTICATION, innerArray[0].asText())
    }

    @Test
    fun `encodeCbor second element is SessionTranscript and not null`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val result = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        val innerArray = decodeTag24Inner(result)
        assertFalse(innerArray[1].isNull)
    }

    @Test
    fun `encodeCbor third element is DocType`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val result = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        val innerArray = decodeTag24Inner(result)
        assertEquals(MDL_DOC_TYPE, innerArray[2].asText())
    }

    @Test
    fun `encodeCbor fourth element is DeviceNameSpacesBytes`() {
        val deviceNameSpacesBytes = encodeDeviceNameSpacesBytes()
        val result = DeviceAuthentication(
            sessionTranscript = validSessionTranscript,
            docType = MDL_DOC_TYPE,
            deviceNameSpacesBytes = deviceNameSpacesBytes
        ).encode()

        val innerArray = decodeTag24Inner(result)
        val nameSpacesInner = (innerArray[3] as BinaryNode).binaryValue()
        val map: Map<*, *> = cborMapper.readValue(nameSpacesInner, Map::class.java)
        assertEquals(0, map.size)
    }

    @Test
    fun `encodeDeviceNameSpacesBytes inner bytes decode to empty map`() {
        val result = encodeDeviceNameSpacesBytes()

        val innerBytes = extractTag24Bytes(result)
        val map: Map<*, *> = cborMapper.readValue(innerBytes, Map::class.java)
        assertEquals(0, map.size)
    }

    private fun decodeTag24Inner(tag24Bytes: ByteArray): JsonNode {
        val parser = cborMapper.createParser(tag24Bytes)
        parser.nextToken()
        val innerBytes = (cborMapper.readTree<JsonNode>(parser) as BinaryNode).binaryValue()
        return cborMapper.readTree(innerBytes)
    }

    private fun extractTag24Bytes(tag24Bytes: ByteArray): ByteArray {
        val parser = cborMapper.createParser(tag24Bytes)
        parser.nextToken()
        return (cborMapper.readTree<JsonNode>(parser) as BinaryNode).binaryValue()
    }
}
