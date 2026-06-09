package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.CBOR_ARRAY_4
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.CBOR_BSTR_1
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.CBOR_EMPTY_MAP
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.ES256_ALG_LABEL
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.ES256_ALG_VALUE
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.TAG_24_MAJOR
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.TAG_24_VALUE

private const val P256_RAW_SIGNATURE_SIZE = 64

class DeviceSignatureUseCaseImplTest {

    private val logger = mockk<Logger>(relaxed = true)
    private val deviceSignatureUseCase = DeviceSignatureUseCaseImpl(logger)
    private val cborMapper = ObjectMapper(CBORFactory())

    private val signatureBytes = createTestDerSignature()

    private fun createTestDerSignature(): ByteArray {
        // A valid DER-encoded ECDSA signature with 32-byte r and s values
        val r = ByteArray(32) { 0x01 }
        val s = ByteArray(32) { 0x02 }
        // DER: 0x30 <totalLen> 0x02 <rLen> <r> 0x02 <sLen> <s>
        val rDer = byteArrayOf(0x02, r.size.toByte()) + r
        val sDer = byteArrayOf(0x02, s.size.toByte()) + s
        val content = rDer + sDer
        return byteArrayOf(0x30, content.size.toByte()) + content
    }

    private fun encodeDeviceNameSpacesBytes(): ByteArray = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray().let { EmbeddedCbor(it).toCbor() }

    private val result by lazy {
        deviceSignatureUseCase.buildDeviceSignedStructures(signatureBytes)
    }

    @Test
    fun `coseSign1Array starts with 4-element array header`() {
        assertEquals(CBOR_ARRAY_4, result.coseSign1Array[0])
    }

    @Test
    fun `coseSign1Array protected header is bstr-wrapped map with alg ES256`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        val protectedHeaderBytes = tree[0].binaryValue()
        assertNotNull(protectedHeaderBytes)
        val headerTree: JsonNode = cborMapper.readTree(protectedHeaderBytes)
        assertEquals(ES256_ALG_VALUE, headerTree[ES256_ALG_LABEL.toString()].intValue())
    }

    @Test
    fun `coseSign1Array unprotected header is empty map`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        assertTrue(tree[1].isObject)
        assertEquals(0, tree[1].size())
    }

    @Test
    fun `coseSign1Array payload is null per RFC 8152 detached content`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        assertTrue(tree[2].isNull)
    }

    @Test
    fun `coseSign1Array signature element contains the raw r-s signature`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        val sig = tree[3].binaryValue()
        assertNotNull(sig)
        assertEquals(P256_RAW_SIGNATURE_SIZE, sig.size)
        // r = 32 bytes of 0x01, s = 32 bytes of 0x02
        assertTrue(sig.copyOfRange(0, 32).all { it == 0x01.toByte() })
        assertTrue(sig.copyOfRange(32, 64).all { it == 0x02.toByte() })
    }

    @Test
    fun `deviceAuth is a 1-entry map with deviceSignature key`() {
        val map: Map<*, *> = cborMapper.readValue(result.deviceAuth, Map::class.java)
        assertEquals(1, map.size)
        assertTrue(map.containsKey("deviceSignature"))
    }

    @Test
    fun `deviceAuth deviceSignature value is the coseSign1Array`() {
        val keyLength = "deviceSignature".toByteArray().size
        val valueOffset = 1 + 1 + keyLength
        val embeddedSignature = result.deviceAuth.copyOfRange(valueOffset, result.deviceAuth.size)
        assertTrue(embeddedSignature.contentEquals(result.coseSign1Array))
    }

    @Test
    fun `deviceSigned is a 2-entry map with nameSpaces and deviceAuth keys`() {
        val map: Map<*, *> = cborMapper.readValue(result.deviceSigned, Map::class.java)
        assertEquals(2, map.size)
        assertTrue(map.containsKey("nameSpaces"))
        assertTrue(map.containsKey("deviceAuth"))
    }

    @Test
    fun `deviceSigned nameSpaces is Tag-24-wrapped empty CBOR map`() {
        val keyLength = "nameSpaces".toByteArray().size
        val valueOffset = 1 + 1 + keyLength
        val nameSpacesBytes = result.deviceSigned.copyOfRange(valueOffset, valueOffset + 4)
        assertEquals(TAG_24_MAJOR, nameSpacesBytes[0])
        assertEquals(TAG_24_VALUE, nameSpacesBytes[1])
        assertEquals(CBOR_BSTR_1, nameSpacesBytes[2])
        assertEquals(CBOR_EMPTY_MAP, nameSpacesBytes[3])
    }

    @Test
    fun `deviceSigned deviceAuth value is the deviceAuth bytes`() {
        val nameSpacesKeyLength = "nameSpaces".toByteArray().size
        val nameSpacesBytesLength = encodeDeviceNameSpacesBytes().size
        val deviceAuthKeyLength = "deviceAuth".toByteArray().size
        val valueOffset =
            1 + 1 + nameSpacesKeyLength + nameSpacesBytesLength + 1 + deviceAuthKeyLength
        val embeddedAuth = result.deviceSigned.copyOfRange(valueOffset, result.deviceSigned.size)
        assertTrue(embeddedAuth.contentEquals(result.deviceAuth))
    }

    @Test
    fun `buildCoseSignStructure produces a valid COSE Sig_structure`() {
        val payload = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte())

        val result = deviceSignatureUseCase.buildCoseSignStructure(payload)
        val tree: JsonNode = cborMapper.readTree(result)

        assertTrue(tree.isArray)
        assertEquals(4, tree.size())
        assertEquals("Signature1", tree[0].textValue())
        // protected header: bstr containing {1: -7}
        val protectedHeader = cborMapper.readTree(tree[1].binaryValue())
        assertEquals(
            ES256_ALG_VALUE,
            protectedHeader[ES256_ALG_LABEL.toString()].intValue()
        )
        // external_aad: empty bstr
        assertEquals(0, tree[2].binaryValue().size)
        // payload
        assertTrue(tree[3].binaryValue().contentEquals(payload))
    }

    @Test
    fun `all result fields are non-empty`() {
        assertTrue(result.coseSign1Array.isNotEmpty())
        assertTrue(result.deviceAuth.isNotEmpty())
        assertTrue(result.deviceSigned.isNotEmpty())
    }
}
