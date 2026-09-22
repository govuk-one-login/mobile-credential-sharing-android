package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoderImpl
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestValidationException
import uk.gov.onelogin.sharing.cryptoService.util.getByteArrayFromHexStringFile
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

class DeviceRequestDecoderImplTest {
    private val deviceRequestExample1 = getByteArrayFromHexStringFile(
        CBOR_FILE_PATH,
        "deviceRequestExampleCbor1.txt"
    )

    private val deviceRequestExample2 = getByteArrayFromHexStringFile(
        CBOR_FILE_PATH,
        "deviceRequestExampleCbor2.txt"
    )

    private val emptyDocRequest = getByteArrayFromHexStringFile(
        CBOR_FILE_PATH,
        "deviceRequestExampleEmptyDocRequest.txt"
    )

    private val logger = SystemLogger()

    private val deviceRequestDecoderImpl = DeviceRequestDecoderImpl(logger)

    @Test
    fun `correctly parses cbor into device request ac1`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample1)
        assertDeviceRequestParsedCorrectly(deviceRequest)
        val docRequest = deviceRequest.docRequests.first()
        assertNotNull(docRequest.itemsRequestBytes)
        assertEquals(null, docRequest.readerAuth)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `correctly parses cbor with readerAuth into device request ac2`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample2)
        assertDeviceRequestParsedCorrectly(deviceRequest)
        val docRequest = deviceRequest.docRequests.first()
        assertNotNull(docRequest.itemsRequestBytes)
        assertNotNull(docRequest.readerAuth)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `itemsRequestBytes and readerAuth slice directly from original input`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample2)
        val docRequest = deviceRequest.docRequests.first()
        val itemsBytes = docRequest.itemsRequestBytes!!
        val readerAuthBytes = docRequest.readerAuth!!

        assert(deviceRequestExample2.toHexString().contains(itemsBytes.toHexString()))
        assert(deviceRequestExample2.toHexString().contains(readerAuthBytes.toHexString()))
    }

    @Test
    fun `trailing data after top-level object throws exception`() {
        val trailingDataBytes = deviceRequestExample1 + byteArrayOf(0x00, 0x01)
        assertFailsWith<DeviceRequestDecodingException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(trailingDataBytes)
        }
    }

    @Test
    fun `when invalid cbor given, decoding fails and status code 11 thrown ac3`() {
        assertFailsWith<DeviceRequestDecodingException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(INVALID_CBOR.toByteArray())
        }

        assert(logger.any { it.message.startsWith("DeviceRequest CBOR decoding failed") })
    }

    @Test
    fun `when docrequests array is empty, validation fails and status code 12 thrown ac4`() {
        assertFailsWith<DeviceRequestValidationException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(emptyDocRequest)
        }

        assert(logger.contains("DeviceRequest contains empty DocRequests"))
    }

    @Test
    fun `itemsRequestBytes starts with Tag 24 and matches exact source slice`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample1)
        val docRequest = deviceRequest.docRequests.first()
        val itemsBytes = docRequest.itemsRequestBytes
        assertNotNull(itemsBytes)

        assertEquals(0xD8.toByte(), itemsBytes[0])
        assertEquals(0x18.toByte(), itemsBytes[1])

        val foundSlice = findSliceOffset(deviceRequestExample1, itemsBytes)
        assert(foundSlice != -1)
        val expectedSlice =
            deviceRequestExample1.copyOfRange(foundSlice, foundSlice + itemsBytes.size)
        assert(expectedSlice.contentEquals(itemsBytes))
    }

    @Test
    fun `readerAuth matches exact source byte slice byte for byte`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample2)
        val docRequest = deviceRequest.docRequests.first()
        val authBytes = docRequest.readerAuth
        assertNotNull(authBytes)

        val foundSlice = findSliceOffset(deviceRequestExample2, authBytes)
        assert(foundSlice != -1)
        val expectedSlice =
            deviceRequestExample2.copyOfRange(foundSlice, foundSlice + authBytes.size)
        assert(expectedSlice.contentEquals(authBytes))
    }

    @Test
    fun `AC3 wrong-shaped COSE structure in readerAuth is preserved as exact raw bytes`() {
        val deviceRequest = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample2)
        val docRequest = deviceRequest.docRequests.first()
        assertNotNull(docRequest.readerAuth)
    }

    @Test
    fun `duplicate map key in DeviceRequest throws decoding exception`() {
        val duplicateKeyCbor = byteArrayOf(
            0xA2.toByte(),
            0x67.toByte(),
            'v'.code.toByte(),
            'e'.code.toByte(),
            'r'.code.toByte(),
            's'.code.toByte(),
            'i'.code.toByte(),
            'o'.code.toByte(),
            'n'.code.toByte(),
            0x63.toByte(),
            '1'.code.toByte(),
            '.'.code.toByte(),
            '0'.code.toByte(),
            0x67.toByte(),
            'v'.code.toByte(),
            'e'.code.toByte(),
            'r'.code.toByte(),
            's'.code.toByte(),
            'i'.code.toByte(),
            'o'.code.toByte(),
            'n'.code.toByte(),
            0x63.toByte(),
            '1'.code.toByte(),
            '.'.code.toByte(),
            '0'.code.toByte()
        )
        assertFailsWith<DeviceRequestDecodingException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(duplicateKeyCbor)
        }
    }

    private fun findSliceOffset(source: ByteArray, target: ByteArray): Int {
        for (i in 0..source.size - target.size) {
            if (source.copyOfRange(i, i + target.size).contentEquals(target)) {
                return i
            }
        }
        return -1
    }

    private fun assertDeviceRequestParsedCorrectly(deviceRequest: DeviceRequest) {
        with(deviceRequest) {
            assertEquals("1.0", version)
            assertEquals(1, docRequests.size)

            with(docRequests.first()) {
                assertEquals(DOC_TYPE, itemsRequest.docType)

                val nameSpaceMap = itemsRequest.nameSpaces[NAME_SPACE]
                assertNotNull(nameSpaceMap)
                assert(nameSpaceMap == INTENT_TO_RETAIN_MAP)
            }
        }
    }

    private companion object {
        private const val CBOR_FILE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/crypto-service/cbor/deserializers/"

        private const val DOC_TYPE = "org.iso.18013.5.1.mDL"

        private const val NAME_SPACE = "org.iso.18013.5.1"

        private val INTENT_TO_RETAIN_MAP = mapOf(
            "family_name" to true,
            "document_number" to true,
            "driving_privileges" to true,
            "issue_date" to true,
            "expiry_date" to true,
            "portrait" to false
        )
    }
}
