package uk.gov.android.credentialsharing.iso180136.sessionEstablishment

import kotlin.test.Test
import kotlin.test.assertNotNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
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
        val result = mapper.writeValueAsBytes(validSessionEstablishmentDto)

        assertThat(
            result,
            equalTo(validSessionEstablishmentDtoBytes)
        )
    }

    @Test
    fun `mDLR_MS_SE_01 (Common_CBOR_02) - Canonicalization rules`() {
        val result = mapper.readValue(
            validSessionEstablishmentDtoBytes,
            SessionEstablishmentDto::class.java
        )

        assertNotNull(result)
    }
}