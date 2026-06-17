package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DIGEST_ALGORITHM
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DOC_TYPE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_EXPECTED_UPDATE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_NAMESPACE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_SIGNED
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VALID_FROM
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VALID_UNTIL
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VERSION
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.buildMsoBytes
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

@OptIn(ExperimentalTime::class)
class MsoDtoTest {
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `deserializes valid MSO bytes into MsoDto`() {
        val bytes = buildMsoBytes()
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        assertThat(dto.version, equalTo(DEFAULT_VERSION))
        assertThat(dto.digestAlgorithm, equalTo(DEFAULT_DIGEST_ALGORITHM))
        assertThat(dto.docType, equalTo(DEFAULT_DOC_TYPE))
        assertNotNull(dto.valueDigests[DEFAULT_NAMESPACE])
        assertNotNull(dto.deviceKeyInfo)
        assertThat(dto.validityInfo.signed, equalTo(Instant.parse(DEFAULT_SIGNED)))
        assertThat(dto.validityInfo.validFrom, equalTo(Instant.parse(DEFAULT_VALID_FROM)))
        assertThat(dto.validityInfo.validUntil, equalTo(Instant.parse(DEFAULT_VALID_UNTIL)))
        assertNull(dto.validityInfo.expectedUpdate)
        assertNull(dto.status)
    }

    @Test
    fun `deserializes MSO with expectedUpdate`() {
        val bytes = buildMsoBytes(includeExpectedUpdate = DEFAULT_EXPECTED_UPDATE)
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        assertThat(
            dto.validityInfo.expectedUpdate,
            equalTo(Instant.parse(DEFAULT_EXPECTED_UPDATE))
        )
    }

    @Test
    fun `deserializes MSO with status field`() {
        val statusBytes = byteArrayOf(0x01, 0x02)
        val bytes = buildMsoBytes(includeStatus = statusBytes)
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        assertNotNull(dto.status)
    }

    @Test
    fun `deserializes MSO with multiple namespaces`() {
        val bytes = buildMsoBytes(
            valueDigests = mapOf(
                DEFAULT_NAMESPACE to mapOf(0 to byteArrayOf(0x01)),
                "org.iso.18013.5.1.aamva" to mapOf(1 to byteArrayOf(0x02))
            )
        )
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        assertThat(dto.valueDigests.keys.size, equalTo(2))
        assertNotNull(dto.valueDigests[DEFAULT_NAMESPACE])
        assertNotNull(dto.valueDigests["org.iso.18013.5.1.aamva"])
    }

    @Test
    fun `deserializes MSO with multiple digest IDs per namespace`() {
        val bytes = buildMsoBytes(
            valueDigests = mapOf(
                DEFAULT_NAMESPACE to mapOf(
                    0 to byteArrayOf(0x01),
                    1 to byteArrayOf(0x02),
                    2 to byteArrayOf(0x03)
                )
            )
        )
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        assertThat(dto.valueDigests[DEFAULT_NAMESPACE]?.size, equalTo(3))
    }

    @Test
    fun `toDomain maps all fields correctly`() {
        val bytes = buildMsoBytes(includeExpectedUpdate = DEFAULT_EXPECTED_UPDATE)
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)
        val mso = dto.toDomain()

        assertThat(mso.version, equalTo(DEFAULT_VERSION))
        assertThat(mso.digestAlgorithm, equalTo(DEFAULT_DIGEST_ALGORITHM))
        assertThat(mso.docType, equalTo(DEFAULT_DOC_TYPE))
        assertNotNull(mso.valueDigests[DEFAULT_NAMESPACE])
        assertNotNull(mso.deviceKeyInfo.deviceKey)
        assertThat(mso.validityInfo.signed, equalTo(Instant.parse(DEFAULT_SIGNED)))
        assertThat(mso.validityInfo.validFrom, equalTo(Instant.parse(DEFAULT_VALID_FROM)))
        assertThat(mso.validityInfo.validUntil, equalTo(Instant.parse(DEFAULT_VALID_UNTIL)))
        assertThat(
            mso.validityInfo.expectedUpdate,
            equalTo(Instant.parse(DEFAULT_EXPECTED_UPDATE))
        )
    }

    @Test
    fun `toDomain throws INVALID_MSO_VERSION for unsupported version`() {
        val bytes = buildMsoBytes(version = "2.0")
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            dto.toDomain()
        }
        assertThat(exception, hasError(VerificationError.INVALID_MSO_VERSION))
    }

    @Test
    fun `toDomain throws UNSUPPORTED_DIGEST_ALGORITHM for non-SHA-256`() {
        val bytes = buildMsoBytes(digestAlgorithm = "SHA-512")
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            dto.toDomain()
        }
        assertThat(exception, hasError(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM))
    }

    @Test
    fun `toDomain maps status bytes when present`() {
        val bytes = buildMsoBytes(includeStatus = byteArrayOf(0xAA.toByte()))
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)
        val mso = dto.toDomain()

        assertNotNull(mso.status)
    }

    @Test
    fun `toDomain maps null status when absent`() {
        val bytes = buildMsoBytes()
        val dto = cborMapper.readValue(bytes, MsoDto::class.java)
        val mso = dto.toDomain()

        assertNull(mso.status)
    }

    @Test
    fun `deserialization throws MALFORMED_MSO for invalid timestamp format`() {
        val bytes = buildMsoBytes(signed = "2024-01-15T10:00:00+00:00")

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            cborMapper.readValue(bytes, MsoDto::class.java)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `deserialization throws MALFORMED_MSO for non-integer digest keys`() {
        val bytes = uk.gov.onelogin.sharing.verification.format.document
            .MobileSecurityObjectStubs.buildMsoBytesWithNonIntegerDigestKeys()

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            cborMapper.readValue(bytes, MsoDto::class.java)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }
}
