package uk.gov.android.credentialsharing.iso180136.sessionEstablishment

import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto.Companion.DATA_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto.Companion.E_READER_KEY_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDtoBytes

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for Session Establishment.
 *
 * These tests validate that the mdoc reader correctly ignores unknown/RFU keys
 * in the SessionEstablishment CBOR structure per ISO/IEC 18013-5:2021, 9.1.1.4.
 *
 * ```
 * SessionEstablishment = {
 * "eReaderKey" : EReaderKeyBytes,
 * "data" : bstr ; Encrypted mdoc request
 * * tstr => RFU
 * }
 * ```
 */
@RunWith(TestParameterInjector::class)
class SessionEstablishmentMessageStructureTest {
    private val mapper = CborMapper.default

    private var sessionEstablishmentDto = validSessionEstablishmentDto
    private val result by lazy {
        mapper.writeValueAsBytes(sessionEstablishmentDto)
    }
    private val resultHexString by lazy {
        result.toHexString()
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `Valid CBOR is decodable`() {
        val result = mapper.readValue(
            validSessionEstablishmentDtoBytes,
            SessionEstablishmentDto::class.java
        )

        assertNotNull(result)
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `Deserialization creates valid CBOR`() {
        assertThat(
            result,
            equalTo(validSessionEstablishmentDtoBytes)
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
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
            resultHexString.chunked(2),
            not(contains(assertion))
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
     * sub-scenario: Common_CBOR_03
     */
    @Test
    fun `There are no duplicate fields`(
        @TestParameter propertyName: String = testValues(
            E_READER_KEY_KEY,
            DATA_KEY
        )
    ) {
        val values = mapper.readTree(result).findValues(propertyName)
        assertThat(
            values,
            hasSize(1)
        )
    }

    /**
     * Scenario: mDLR_MS_SE_02
     */
    @Test
    fun `CBOR structure is a map of 2 elements`() {
        assertThat(
            resultHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2))
        )
    }

    /**
     * Scenario: mDLR_MS_SE_02
     */
    @Test
    fun `eReaderKey is an embedded CBOR object with 4 fields`() {
        val eReaderKeyPrefix = "eReaderKey".toByteArray().toHexString()
        val embeddedCborPaddingStart = "d818584b"

        val expectedEReaderPrefix = eReaderKeyPrefix +
                embeddedCborPaddingStart +
                HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 4)

        assertThat(
            resultHexString,
            containsString(expectedEReaderPrefix),
        )
    }

    /**
     * Scenario: mDLR_MS_SE_03
     */
    @Test
    fun `Hex string elements are the correct data type`() {
        val dataPrefix = "data".toByteArray().toHexString()
        val hStringPrefix = "5902df"

        assertThat(
            resultHexString,
            allOf(
                containsString(dataPrefix + hStringPrefix)
            )
        )
    }
}
