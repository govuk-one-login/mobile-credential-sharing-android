package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceRequest
import uk.gov.onelogin.sharing.security.cbor.dto.devicerequest.DeviceRequestDto
import uk.gov.onelogin.sharing.security.util.getByteArrayFromHexStringFile

class DeviceRequestDeserializerTest {
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

    @Test
    fun `correctly parses cbor into device request ac1`() {
        val deviceRequestDto = decodeDeviceRequest(deviceRequestExample1, logger)
        assertDeviceRequestParsedCorrectly(deviceRequestDto)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `correctly parses  cbor into device request ac2`() {
        val deviceRequestDto = decodeDeviceRequest(deviceRequestExample2, logger)
        assertDeviceRequestParsedCorrectly(deviceRequestDto)
        assert(logger.contains("device request decoded successfully"))
    }

    @Test
    fun `when invalid cbor given, decoding fails and status code 11 thrown ac3`() {
        assertFailsWith<MismatchedInputException> {
            decodeDeviceRequest(INVALID_CBOR.toByteArray(), logger)
        }

        assert(logger.contains("session termination: status code 11"))
    }

    @Test
    fun `when docrequests array is empty, decoding fails and status code 20 thrown ac4`() {
        assertFailsWith<TypeNotPresentException> {
            decodeDeviceRequest(emptyDocRequest, logger)
        }

        assert(logger.contains("empty DocRequest: status code 20"))
    }

    private fun assertDeviceRequestParsedCorrectly(deviceRequestDto: DeviceRequestDto) {
        with(deviceRequestDto) {
            assertEquals("1.0", version)
            assertEquals(1, docRequest.size)
            assertNull(deviceRequestInfo)
            assertNull(readerAuthAll)

            with(docRequest.first()) {
                assertNull(readerAuth)
                assertEquals(DOC_TYPE, itemsRequest.docType)

                val nameSpaceMap = itemsRequest.nameSpaces[NAME_SPACE]
                assertNotNull(nameSpaceMap)
                assert(nameSpaceMap == INTENT_TO_RETAIN_MAP)

                assertNull(itemsRequest.requestInfo)
            }
        }
    }

    private companion object {
        private const val CBOR_FILE_PATH =
            "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cbor/deserializers/"

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
