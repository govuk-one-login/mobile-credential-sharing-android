package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoderImpl
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
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
        val deviceRequestDto = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample1)
        assertDeviceRequestParsedCorrectly(deviceRequestDto)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `correctly parses  cbor into device request ac2`() {
        val deviceRequestDto = deviceRequestDecoderImpl.deviceRequestDecoder(deviceRequestExample2)
        assertDeviceRequestParsedCorrectly(deviceRequestDto)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `when invalid cbor given, decoding fails and status code 11 thrown ac3`() {
        assertFailsWith<DeviceRequestDecodingException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(INVALID_CBOR.toByteArray())
        }

        assert(logger.contains("session termination: status code 11"))
    }

    @Test
    fun `when docrequests array is empty, decoding fails and status code 20 thrown ac4`() {
        assertFailsWith<DeviceRequestDecodingException> {
            deviceRequestDecoderImpl.deviceRequestDecoder(emptyDocRequest)
        }

        assert(logger.contains("empty DocRequest: status code 20"))
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
