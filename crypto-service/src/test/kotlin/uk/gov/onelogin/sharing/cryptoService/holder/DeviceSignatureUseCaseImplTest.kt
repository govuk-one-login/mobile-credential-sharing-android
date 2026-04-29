package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.CBOR_ARRAY_4
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.CBOR_EMPTY_MAP
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.COSE_SIGN1_TAG
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.ES256_ALG_LABEL
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.ES256_ALG_VALUE
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.TAG_24_MAJOR
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCaseStub.TAG_24_VALUE

class DeviceSignatureUseCaseImplTest {

    private val logger = mockk<Logger>(relaxed = true)
    private val deviceSignatureUseCase = DeviceSignatureUseCaseImpl(logger)
    private val holderCryptoService = HolderCryptoServiceImpl(
        sessionSecurity = FakeSessionSecurity(),
        logger = SystemLogger()
    )
    private val cborMapper = ObjectMapper(CBORFactory())

    private val signatureBytes = holderCryptoService.buildDeviceAuthenticationBytes(
        sessionTranscript = validSessionTranscript,
        docType = MDL_DOC_TYPE
    ).deviceAuthenticationBytes

    private val result by lazy { deviceSignatureUseCase.buildDeviceSignedStructures(signatureBytes) }

    @Test
    fun `coseSign1Array starts with COSE_Sign1 tag 18`() {
        assertEquals(COSE_SIGN1_TAG, result.coseSign1Array[0])
    }

    @Test
    fun `coseSign1Array has 4-element array header after tag`() {
        assertEquals(CBOR_ARRAY_4, result.coseSign1Array[1])
    }

    @Test
    fun `coseSign1Array protected header is bstr-wrapped map with alg ES256`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        val protectedHeaderBytes = tree[0].binaryValue()
        assertNotNull(protectedHeaderBytes)
        val headerMap: Map<*, *> = cborMapper.readValue(protectedHeaderBytes, Map::class.java)
        assertEquals(ES256_ALG_VALUE, headerMap[ES256_ALG_LABEL])
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
    fun `coseSign1Array signature element contains the provided signature bytes`() {
        val tree: JsonNode = cborMapper.readTree(result.coseSign1Array)
        val sig = tree[3].binaryValue()
        assertNotNull(sig)
        assertTrue(sig.contentEquals(signatureBytes))
    }

    @Test
    fun `deviceAuth is a 1-entry map with deviceSignature key`() {
        val map: Map<*, *> = cborMapper.readValue(result.deviceAuth, Map::class.java)
        assertEquals(1, map.size)
        assertTrue(map.containsKey("deviceSignature"))
    }

    @Test
    fun `deviceAuth deviceSignature value is the coseSign1Array`() {
        val map: Map<*, *> = cborMapper.readValue(result.deviceAuth, Map::class.java)
        val embeddedSignature = map["deviceSignature"] as ByteArray
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
        val map: Map<*, *> = cborMapper.readValue(result.deviceSigned, Map::class.java)
        val nameSpacesBytes = map["nameSpaces"] as ByteArray
        assertEquals(TAG_24_MAJOR, nameSpacesBytes[0])
        assertEquals(TAG_24_VALUE, nameSpacesBytes[1])
        val wrappedLength = nameSpacesBytes[2].toInt() and 0xff
        assertEquals(1, wrappedLength)
        assertEquals(CBOR_EMPTY_MAP, nameSpacesBytes[3])
    }

    @Test
    fun `deviceSigned deviceAuth value is the deviceAuth bytes`() {
        val map: Map<*, *> = cborMapper.readValue(result.deviceSigned, Map::class.java)
        val embeddedAuth = map["deviceAuth"] as ByteArray
        assertTrue(embeddedAuth.contentEquals(result.deviceAuth))
    }

    @Test
    fun `all result fields are non-empty`() {
        assertTrue(result.coseSign1Array.isNotEmpty())
        assertTrue(result.deviceAuth.isNotEmpty())
        assertTrue(result.deviceSigned.isNotEmpty())
    }
}
