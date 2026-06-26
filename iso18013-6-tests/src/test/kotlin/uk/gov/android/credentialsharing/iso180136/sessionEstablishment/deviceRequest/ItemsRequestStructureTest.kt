package uk.gov.android.credentialsharing.iso180136.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import junit.framework.TestCase.assertTrue
import kotlin.test.Ignore
import kotlin.test.Test
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.DOC_REQUESTS_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.deviceRequestStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto.Companion.ITEMS_REQUEST_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_DOC_TYPE
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_NAMESPACES
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_REQUEST_INFO
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper.default as mapper

@RunWith(TestParameterInjector::class)
class ItemsRequestStructureTest {
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

    private val itemRequestNodes by lazy {
        docRequestNodes.map {
            mapper.readTree(it[ITEMS_REQUEST_KEY].binaryValue())
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_08
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
     * Scenario ID: mDLR_MS_DR_08
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
        ),
    ) {
        assertThat(
            deviceRequestHexString.chunked(2),
            not(Matchers.contains(assertion))
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_08
     * sub-scenario: Common_CBOR_03
     */
    @Test
    fun `There are no duplicate fields`(
        @TestParameter propertyName: String = testValues(
            KEY_DOC_TYPE,
            KEY_NAMESPACES,
        ),
    ) {
        itemRequestNodes.forEach { itemRequest ->
            assertThat(
                itemRequest.findValues(propertyName),
                hasSize(1)
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_08
     * sub-scenario: Common_CBOR_03
     *
     * Fails conformance test due to [ItemsRequestDto.Serializer] and
     * [ItemsRequestDto.Deserializer] ignoring the optional fields when writing / reading CBOR.
     */
    @Test
    @Ignore("Fails conformance test due to incomplete (de)serializer implementation")
    fun `There are no duplicate fields - Ignored fields`(
        @TestParameter propertyName: String = testValues(
            KEY_REQUEST_INFO,
        ),
    ) {
        updateWithRequestInfo()

        itemRequestNodes.forEach { itemRequest ->
            assertThat(
                itemRequest.findValues(propertyName),
                hasSize(1)
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_09
     */
    @Test
    fun `The underlying ItemsRequest objects are maps`() {
        assertTrue(
            itemRequestNodes.all {
                it.isObject
            }
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_10
     */
    @Test
    fun `There is a finite property count for item requests`() {
        itemRequestNodes.forEach { node ->
            assertThat(
                node.properties(),
                hasSize(
                    anyOf(
                        greaterThanOrEqualTo(2),
                        lessThanOrEqualTo(3)
                    )
                )
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_10
     */
    @Test
    fun `Item request properties are of the correct type`(
        @TestParameter input: Pair<String, (JsonNode) -> Boolean> = namedTestValues(
            "Document type" to (KEY_DOC_TYPE to JsonNode::isTextual),
            "Namespaces" to (KEY_NAMESPACES to JsonNode::isObject),
        )
    ) {
        val (property, assertion) = input

        itemRequestNodes.all { node ->
            assertion(node[property])
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_10
     *
     * Fails conformance test due to [ItemsRequestDto.Serializer] and
     * [ItemsRequestDto.Deserializer] ignoring the optional fields when writing / reading CBOR.
     */
    @Test
    @Ignore("Fails conformance test due to incomplete (de)serializer implementation")
    fun `Item request properties are of the correct type - Ignored fields`(
        @TestParameter input: Pair<String, (JsonNode) -> Boolean> = namedTestValues(
            "Request info" to (KEY_REQUEST_INFO to JsonNode::isBinary)
        )
    ) {
        val (property, assertion) = input

        updateWithRequestInfo()

        itemRequestNodes.all { node ->
            assertion(node[property])
        }
    }

    private fun updateWithRequestInfo(
        requestInfo: ByteArray = byteArrayOf(1, 2)
    ) {
        deviceRequest = deviceRequest.copy(
            docRequest = listOf(
                deviceRequest.docRequest[0].copy(
                    itemsRequest = deviceRequest.docRequest[0].itemsRequest.copy(
                        requestInfo = requestInfo
                    )
                )
            )
        )
    }
}
