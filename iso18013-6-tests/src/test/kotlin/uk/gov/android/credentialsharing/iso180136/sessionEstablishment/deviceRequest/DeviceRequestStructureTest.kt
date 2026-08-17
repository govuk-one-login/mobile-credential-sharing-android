package uk.gov.android.credentialsharing.iso180136.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper.default as mapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.DEVICE_REQUEST_INFO_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.DOC_REQUESTS_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.READER_AUTH_ALL_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.VERSION_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoMatchers.hasVersion
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.deviceRequestStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto.Companion.ITEMS_REQUEST_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto.Companion.READER_AUTH_KEY

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for Session Establishment.
 *
 * ```
 * DeviceRequest = {
 *    "version" : Version,
 *    "docRequests" : [+ DocRequest],
 *    ? "deviceRequestInfo" : DeviceRequestInfoBytes,
 *    ? "readerAuthAll" : [+ReaderAuthAll],
 *    * tstr => RFU
 * }
 * ```
 */
@RunWith(TestParameterInjector::class)
class DeviceRequestStructureTest {
    private var deviceRequest = deviceRequestStub

    private val deviceRequestBytes by lazy {
        mapper.writeValueAsBytes(deviceRequest)
    }

    private val deviceRequestHexString by lazy {
        deviceRequestBytes.toHexString()
    }

    private val docRequestNodes by lazy {
        mapper.readTree(deviceRequestBytes).withArrayProperty(DOC_REQUESTS_KEY)
    }

    /**
     * Scenario ID: mDLR_MS_DR_01
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `Valid CBOR is decodable`() {
        val dto = mapper.readValue(
            deviceRequestBytes,
            DeviceRequestDto::class.java
        )

        assertThat(
            dto,
            not(nullValue())
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_01
     * sub-scenario: Common_CBOR_02
     */
    @Test
    fun `There are no indefinite length objects`(
        @TestParameter assertion: Matcher<in String> = namedTestValues(
            "Indefinite length byte strings" to containsString(
                BYTE_STRING_INDEFINITE.toHexString()
            ),
            "Indefinite byte objects" to containsString(
                BYTE_OBJECT_INDEFINITE.toHexString()
            ),
            "Indefinite byte arrays" to containsString(
                (PREFIX_TYPE_BYTES + SUFFIX_INDEFINITE).toHexString()
            )
        )
    ) {
        assertThat(
            deviceRequestHexString.chunked(2),
            not(Matchers.contains(assertion))
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_01
     * sub-scenario: Common_CBOR_03
     */
    @Test
    fun `There are no duplicate fields`(
        @TestParameter propertyName: String = testValues(
            VERSION_KEY,
            DOC_REQUESTS_KEY
        )
    ) {
        val values = mapper.readTree(deviceRequestBytes).findValues(propertyName)
        assertThat(
            values,
            hasSize(1)
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_01
     * sub-scenario: Common_CBOR_03
     */
    //    @Ignore("Fails conformance test due to incomplete (de)serializer implementation")
    @Test
    fun `There are no duplicate fields - Ignored fields`(
        @TestParameter propertyName: String = testValues(
            DEVICE_REQUEST_INFO_KEY,
            READER_AUTH_ALL_KEY
        )
    ) {
        deviceRequest = deviceRequest.copy(
            deviceRequestInfo = byteArrayOf(0, 1),
            readerAuthAll = byteArrayOf(1, 2)
        )

        val values = mapper.readTree(deviceRequestBytes).findValues(propertyName)
        assertThat(
            values,
            hasSize(1)
        )
    }

    /**
     * Scenario: mDLR_MS_SE_02, mDLR_MS_DR_03
     */
    @Test
    fun `CBOR structure is a map of 2 elements`() {
        assertThat(
            deviceRequestHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2))
        )
    }

    /**
     * Scenario: mDLR_MS_DR_03
     */
    @Test
    fun `CBOR has the applicable properties`(
        @TestParameter input: Pair<String, (JsonNode) -> Boolean> = namedTestValues(
            "Version" to (
                VERSION_KEY to JsonNode::isTextual
                ),
            "Document request" to (
                DOC_REQUESTS_KEY to JsonNode::isArray
                )
        )
    ) {
        val (property, assertion) = input
        val rootNode = mapper.readTree(deviceRequestBytes)
        val result = rootNode.get(property)

        assertTrue(assertion(result))
    }

    /**
     * Scenario: mDLR_MS_DR_04
     */
    @Test
    fun `Major version can only be 1, with minor version 0`() {
        val result = mapper.readValue(deviceRequestBytes, DeviceRequestDto::class.java)

        assertThat(
            result,
            hasVersion("1.0")
        )
    }

    /**
     * Scenario: mDLR_MS_DR_04
     *
     * Fails conformance tests due to [DeviceRequestDto] `init` block only validating that the
     * version isn't empty.
     */
    @Test
    @Ignore("Fails conformance test due to minimal input validation")
    fun `Invalid versions throw exceptions`(
        @TestParameter version: String = namedTestValues(
            "Empty version" to "",
            "Negative versions" to "-1",
            "Zero version" to "0",
            "Major version 2" to "2.0",
            "Minor version 1" to "1.1",
            "Semantic versioning" to "1.0.0"
        )
    ) {
        assertThrows(IllegalArgumentException::class.java) {
            deviceRequest = deviceRequest.copy(
                version = version
            )
        }
    }

    /**
     * Scenario: mDLR_MS_DR_05
     *
     * Fails conformance test due to [DeviceRequestDto] `init` block performing no validation on
     * [DeviceRequestDto.docRequest].
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `Document requests cannot be empty`() {
        assertThrows(IllegalArgumentException::class.java) {
            deviceRequest = deviceRequest.copy(
                docRequest = emptyList()
            )
        }
    }

    /**
     * Scenario: mDLR_MS_DR_05
     */
    @Test
    fun `Every document request is an object`() {
        assertTrue(docRequestNodes.all(JsonNode::isObject))

        val requests = docRequestNodes.map {
            mapper.treeToValue(it, DocRequestDto::class.java)
        }

        assertThat(
            requests,
            equalTo(deviceRequest.docRequest)
        )
    }

    /**
     * Scenario: mDLR_MS_DR_06
     */
    @Test
    fun `Each document request item has an 'itemsRequest' property`() {
        assertTrue(
            docRequestNodes.all {
                it.has(ITEMS_REQUEST_KEY)
            }
        )
    }

    /**
     * Scenario: mDLR_MS_DR_06
     *
     * Fails conformance test due to [DocRequestDto.Serializer] / [DocRequestDto.Deserializer] not
     * writing the [DocRequestDto.readerAuth] property.
     */
    @Test
    @Ignore("Fails conformance test due to incomplete (de)serializer implementation")
    fun `Some document requests can have a 'readerAuth' property`() {
        deviceRequest = deviceRequest.copy(
            docRequest = listOf(
                deviceRequest.docRequest[0].copy(
                    readerAuth = byteArrayOf(1, 2)
                )
            )
        )

        assertTrue(
            docRequestNodes.any {
                it.has(READER_AUTH_KEY)
            }
        )
    }

    /**
     * Scenario: mDLR_MS_DR_07
     */
    @Test
    fun `Document request items are encoded CBOR data items`() {
        val prefix = ITEMS_REQUEST_KEY.toByteArray().toHexString() +
            "d818584b"

        assertThat(
            deviceRequestHexString,
            containsString(prefix)
        )
    }
}
