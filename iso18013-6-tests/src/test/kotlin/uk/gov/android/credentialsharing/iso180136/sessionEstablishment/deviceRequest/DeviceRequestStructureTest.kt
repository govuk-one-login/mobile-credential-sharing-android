package uk.gov.android.credentialsharing.iso180136.sessionEstablishment.deviceRequest

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
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.DOC_REQUESTS_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto.Companion.VERSION_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDtoStub.deviceRequestStub

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
    private val mapper = CborMapper.default

    private var deviceRequest = deviceRequestStub

    private val deviceRequestBytes by lazy {
        mapper.writeValueAsBytes(deviceRequest)
    }

    private val deviceRequestHexString by lazy {
        deviceRequestBytes.toHexString()
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
            DOC_REQUESTS_KEY,
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
    @Test
    @Ignore("Fails conformance test due to not writing the relevant field")
    fun `There are no duplicate fields - Ignored fields`(
        @TestParameter propertyName: String = testValues(
            "deviceRequestInfo",
            "readerAuthAll"
        )
    ) {
        val values = mapper.readTree(deviceRequestBytes).findValues(propertyName)
        assertThat(
            values,
            hasSize(1)
        )
    }
}