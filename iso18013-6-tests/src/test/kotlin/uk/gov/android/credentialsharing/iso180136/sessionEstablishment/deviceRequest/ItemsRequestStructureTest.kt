package uk.gov.android.credentialsharing.iso180136.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.io.ByteArrayOutputStream
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
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper.default as mapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.DOC_REQUESTS_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.deviceRequestStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto.Companion.ITEMS_REQUEST_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_DOC_TYPE
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_NAMESPACES
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto.Companion.KEY_REQUEST_INFO
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute

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
        )
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
            KEY_NAMESPACES
        )
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
            KEY_REQUEST_INFO
        )
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
            "Namespaces" to (KEY_NAMESPACES to JsonNode::isObject)
        )
    ) {
        val (property, assertion) = input

        assertTrue(
            itemRequestNodes.all { node ->
                assertion(node[property])
            }
        )
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

        assertTrue(
            itemRequestNodes.all { node ->
                assertion(node[property])
            }
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_11
     */
    @Test
    fun `Item requests enforce valid document types`(
        @TestParameter type: DocumentType = namedTestValues(
            "Mobile Driving Licence" to DocumentType.Mdl
        )
    ) {
        deviceRequest = deviceRequest.copy(
            docRequest = listOf(
                deviceRequest.docRequest[0].copy(
                    itemsRequest = deviceRequest.docRequest[0].itemsRequest.copy(
                        docType = type.value
                    )
                )
            )
        )

        assertThat(
            deviceRequestHexString,
            containsString(type.value.toByteArray().toHexString())
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_11
     *
     * Fails conformance test due to [ItemsRequestDto.docType] validation. Specifically, there's no
     * guard against unknown document types.
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `Invalid item request document types throw exceptions`(
        @TestParameter type: DocumentType = namedTestValues(
            "Unknown document type" to DocumentType.Custom("ISO.spec.test"),
            "Empty document type" to DocumentType.Custom(""),
            "Whitespace document type" to DocumentType.Custom(" \t")
        )
    ) {
        assertThrows(IllegalArgumentException::class.java) {
            deviceRequest = deviceRequest.copy(
                docRequest = listOf(
                    deviceRequest.docRequest[0].copy(
                        itemsRequest = deviceRequest.docRequest[0].itemsRequest.copy(
                            docType = type.value
                        )
                    )
                )
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_12
     */
    @Test
    fun `Item requests must define at least one namespace`() {
        assertThrows(IllegalArgumentException::class.java) {
            updateNamespaces(emptyMap())
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_12
     */
    @Test
    fun `Valid Item request namespaces`(
        @TestParameter namespace: String = namedTestValues(
            "ISO-180136 Core namespace" to DocumentType.Mdl.NAMESPACE,
            "Domestic namespace: Great Britain" to "org.iso.18013.5.1.GB"
        )
    ) {
        updateNamespaces(mapOf(namespace to mapOf(MdlAttribute.Portrait.value to false)))

        assertThat(
            deviceRequestHexString,
            containsString(namespace.toByteArray().toHexString())
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_12
     *
     * Fails conformance test due to no input validation against the keys of
     * [ItemsRequestDto.nameSpaces].
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `Invalid Item request namespaces throw an exception`(
        @TestParameter attribute: String = namedTestValues(
            "Unknown namespace" to "ISO.spec.test",
            "Empty namespace" to "",
            "Whitespace namespace" to " \t"
        )
    ) {
        assertThrows(IllegalArgumentException::class.java) {
            updateNamespaces(
                mapOf(attribute to mapOf(MdlAttribute.Portrait.value to true))
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_13
     *
     * Fails conformance test due to [ItemsRequestDto.nameSpaces] validation. Specifically, there's
     * no validation against the `Map<String, Boolean>` values of [ItemsRequestDto.nameSpaces].
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `Item requests must define at least one attribute`() {
        assertThrows(IllegalArgumentException::class.java) {
            updateNamespaceAttributes(emptyMap())
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_13
     */
    @Test
    fun `Valid Item request attributes`(
        @TestParameter attribute: String = testValues(
            "administrative_number",
            "age_birth_year",
            "biometric_template_face",
            "biometric_template_finger",
            "biometric_template_iris",
            "eye_colour",
            "family_name_national_character",
            "hair_colour",
            "height",
            "issuing_jurisdiction",
            MdlAttribute.AgeOver(18).value,
            MdlAttribute.AgeOver(21).value,
            MdlAttribute.BirthDate.value,
            MdlAttribute.BirthPlace.value,
            MdlAttribute.DocumentNumber.value,
            MdlAttribute.DrivingPrivileges.value,
            MdlAttribute.ExpiryDate.value,
            MdlAttribute.FamilyName.value,
            MdlAttribute.GivenName.value,
            MdlAttribute.IssueDate.value,
            MdlAttribute.IssuingAuthority.value,
            MdlAttribute.IssuingCountry.value,
            MdlAttribute.Portrait.value,
            MdlAttribute.ResidentAddress.value,
            MdlAttribute.ResidentCity.value,
            MdlAttribute.ResidentPostalCode.value,
            MdlAttribute.UnDistinguishingSign.value,
            "nationality",
            "portrait_capture_date",
            "resident_country",
            "resident_postal_code",
            "resident_state",
            "sex",
            "signature_usual_mark",
            "weight"
        )
    ) {
        updateNamespaceAttributes(mapOf(attribute to true))

        assertThat(
            deviceRequestHexString,
            containsString(attribute.toByteArray().toHexString())
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_13
     *
     * Fails conformance test due to [ItemsRequestDto.nameSpaces] validation. Specifically, there's
     * no validation against the `Map<String, Boolean>` values of [ItemsRequestDto.nameSpaces].
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `Invalid Item request attributes throw an exception`(
        @TestParameter attribute: MdlAttribute = namedTestValues(
            "Unknown attribute" to MdlAttribute.Custom("ISO.spec.test"),
            "Empty attribute" to MdlAttribute.Custom(""),
            "Whitespace attribute" to MdlAttribute.Custom(" \t")
        )
    ) {
        assertThrows(IllegalArgumentException::class.java) {
            updateNamespaceAttributes(
                mapOf(attribute.value to true)
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_DR_14
     */
    @Test
    fun `Intent to retain is CBOR encoded`(
        @TestParameter input: Pair<Boolean, String> = testValues(
            false to "f4",
            true to "f5"
        )
    ) {
        val (intentToRetain, encodedIntent) = input
        val attribute = MdlAttribute.Portrait.value
        updateNamespaceAttributes(mapOf(attribute to intentToRetain))

        assertThat(
            deviceRequestHexString,
            containsString(attribute.toByteArray().toHexString() + encodedIntent)
        )
    }

    /**
     * Scenario ID: mDLR_MS_DR_15
     *
     * Fails conformance test due to [ItemsRequestDto.Serializer] and
     * [ItemsRequestDto.Deserializer] ignoring the [ItemsRequestDto.requestInfo] property of type
     * `DocRequestInfo`. Resolving this requires the creation of the `DocRequestInfo` data
     * structure.
     */
    @Test
    @Ignore("Fails conformance test due to incomplete (de)serializer implementation")
    fun `Document request info contains additional information`() {
        val requestInfoBytes = ByteArrayOutputStream().also { output ->
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeStartObject(1)
                gen.writeBooleanField("uniqueDocSetRequired", true)
            }
        }.toByteArray()
        updateWithRequestInfo(requestInfoBytes)

        val dto = mapper.readValue(
            deviceRequestBytes,
            DeviceRequestDto::class.java
        )

        assertThat(
            dto.docRequest[0].itemsRequest.requestInfo?.toHexString(),
            containsString("uniqueDocSetRequired".toByteArray().toHexString())
        )
    }

    @Test
    fun `Option 2 attributes are correctly encoded in their respective namespaces`() {
        val gbNamespace = "org.iso.18013.5.1.GB"
        val attributes = mapOf(
            DocumentType.Mdl.NAMESPACE to mapOf(
                "given_name" to true,
                "age_over_23" to false
            ),
            gbNamespace to mapOf(
                "title" to true
            )
        )
        updateNamespaces(attributes)

        assertThat(
            deviceRequestHexString,
            containsString(DocumentType.Mdl.NAMESPACE.toByteArray().toHexString())
        )

        assertThat(
            deviceRequestHexString,
            containsString("given_name".toByteArray().toHexString() + "f5")
        )

        assertThat(
            deviceRequestHexString,
            containsString("age_over_23".toByteArray().toHexString() + "f4")
        )
        assertThat(deviceRequestHexString, containsString(gbNamespace.toByteArray().toHexString()))

        assertThat(
            deviceRequestHexString,
            containsString("title".toByteArray().toHexString() + "f5")
        )
    }

    private fun updateWithRequestInfo(requestInfo: ByteArray = byteArrayOf(1, 2)) {
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

    private fun updateNamespaces(data: Map<String, Map<String, Boolean>>) {
        deviceRequest = deviceRequest.copy(
            docRequest = listOf(
                deviceRequest.docRequest[0].copy(
                    itemsRequest = deviceRequest.docRequest[0].itemsRequest.copy(
                        nameSpaces = data
                    )
                )
            )
        )
    }

    private fun updateNamespaceAttributes(data: Map<String, Boolean>) {
        deviceRequest = deviceRequest.copy(
            docRequest = listOf(
                deviceRequest.docRequest[0].copy(
                    itemsRequest = deviceRequest.docRequest[0].itemsRequest.copy(
                        nameSpaces = mapOf(
                            DocumentType.Mdl.NAMESPACE to data
                        )
                    )
                )
            )
        )
    }
}
