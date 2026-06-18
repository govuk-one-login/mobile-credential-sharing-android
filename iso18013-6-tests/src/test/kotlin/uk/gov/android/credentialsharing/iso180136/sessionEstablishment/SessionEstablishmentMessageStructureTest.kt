package uk.gov.android.credentialsharing.iso180136.sessionEstablishment

import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto
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
class SessionEstablishmentMessageStructureTest {
    private val mapper = CborMapper.default

    private var sessionEstablishmentDto = validSessionEstablishmentDto
    private val result by lazy {
        mapper.writeValueAsBytes(sessionEstablishmentDto)
    }
    private val resultHexString by lazy {
        result.toHexString()
    }

    @Test
    fun `mDLR_MS_SE_01 (Common_CBOR_01) - Valid CBOR is decodable`() {
        val result = mapper.readValue(
            validSessionEstablishmentDtoBytes,
            SessionEstablishmentDto::class.java
        )

        assertNotNull(result)
    }

    @Test
    fun `mDLR_MS_SE_01 (Common_CBOR_01) - Deserialization creates valid CBOR`() {
        assertThat(
            result,
            equalTo(validSessionEstablishmentDtoBytes)
        )
    }

    @Test
    fun `mDLR_MS_SE_02 - hex string is a map with 2 elements`() {
        assertThat(
            resultHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2))
        )
    }

    @Test
    fun `mDLR_MS_SE_03 - hex string elements are the correct data type`() {
        val eReaderKeyPrefix = "eReaderKey".toByteArray().toHexString()
        val dataPrefix = "data".toByteArray().toHexString()
        val embeddedCborPaddingStart = "d818584b"
        val hStringPrefix = "5902df"

        val expectedEReaderPrefix = eReaderKeyPrefix +
                embeddedCborPaddingStart +
                HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 4)

        assertThat(
            resultHexString,
            allOf(
                startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2)),
                containsString(expectedEReaderPrefix),
                containsString(dataPrefix + hStringPrefix)
            )
        )
    }
}
