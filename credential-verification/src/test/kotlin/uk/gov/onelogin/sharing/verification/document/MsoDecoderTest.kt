package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DIGEST_ALGORITHM
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DOC_TYPE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_EXPECTED_UPDATE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_NAMESPACE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_SIGNED
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VALID_FROM
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VALID_UNTIL
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VERSION
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoNotTag24Wrapped
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithDuplicateKeys
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithEmptyDeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithExpectedUpdate
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithFractionalSeconds
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithInvalidExpectedUpdate
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingDeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingDigestAlgorithm
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingDocType
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingValidityInfo
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingValueDigests
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMissingVersion
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMultipleNamespaces
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithNonIntegerDigestKeys
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithNumericOffset
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithStatus
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithUnsupportedAlgorithm
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.malformedEncodedMSO
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.validEncodedMSO
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

@OptIn(ExperimentalTime::class)
@RunWith(TestParameterInjector::class)
class MsoDecoderTest {
    private val decoder = MsoDecoder()

    /**
     * AC1: Valid MSO decodes to correct structure
     */
    @Test
    fun `valid MSO decodes to correct docType`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.docType, equalTo(DEFAULT_DOC_TYPE))
    }

    @Test
    fun `valid MSO decodes to correct version`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.version, equalTo(DEFAULT_VERSION))
    }

    @Test
    fun `valid MSO decodes to correct digestAlgorithm`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.digestAlgorithm, equalTo(DEFAULT_DIGEST_ALGORITHM))
    }

    @Test
    fun `valid MSO decodes valueDigests with correct namespace`() {
        val mso = decoder.decode(validEncodedMSO)
        assertNotNull(mso.valueDigests[DEFAULT_NAMESPACE])
    }

    @Test
    fun `valid MSO decodes deviceKeyInfo`() {
        val mso = decoder.decode(validEncodedMSO)
        assertNotNull(mso.deviceKeyInfo.deviceKey)
    }

    @Test
    fun `valid MSO decodes validityInfo with UTC Z timestamps`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.validityInfo.signed, equalTo(Instant.parse(DEFAULT_SIGNED)))
        assertThat(mso.validityInfo.validFrom, equalTo(Instant.parse(DEFAULT_VALID_FROM)))
        assertThat(mso.validityInfo.validUntil, equalTo(Instant.parse(DEFAULT_VALID_UNTIL)))
    }

    /**
     * AC2: Non-decodable bytes throw MALFORMED_MSO
     */
    @Test
    fun `decode throws MALFORMED_MSO for non-decodable bytes`(
        @TestParameter input: ByteArray = testValues(
            malformedEncodedMSO,
            byteArrayOf(),
            byteArrayOf(0x01, 0x02, 0x03, 0x04)
        )
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(input)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    /**
     * AC3: Invalid timestamp format throws MALFORMED_MSO
     */
    @Test
    fun `decode throws MALFORMED_MSO for invalid timestamp format`(
        @TestParameter input: ByteArray = testValues(
            encodedMsoWithNumericOffset,
            encodedMsoWithFractionalSeconds
        )
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(input)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    /**
     * AC4: Duplicate CBOR keys throw MALFORMED_MSO
     */
    @Test
    fun `decode throws MALFORMED_MSO for duplicate keys`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoWithDuplicateKeys)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `decode throws MALFORMED_MSO when required field is missing`(
        @TestParameter input: ByteArray = testValues(
            encodedMsoWithMissingVersion,
            encodedMsoWithMissingDigestAlgorithm,
            encodedMsoWithMissingDocType,
            encodedMsoWithMissingValueDigests,
            encodedMsoWithMissingDeviceKeyInfo,
            encodedMsoWithMissingValidityInfo
        )
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(input)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `decode throws MALFORMED_MSO for non-integer digest keys`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoWithNonIntegerDigestKeys)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `decode throws MALFORMED_MSO when deviceKeyInfo has no deviceKey`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoWithEmptyDeviceKeyInfo)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `decode throws MALFORMED_MSO when input is not tag24 wrapped`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoNotTag24Wrapped)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `valid MSO decodes expectedUpdate when present`() {
        val mso = decoder.decode(encodedMsoWithExpectedUpdate)
        assertThat(mso.validityInfo.expectedUpdate, equalTo(Instant.parse(DEFAULT_EXPECTED_UPDATE)))
    }

    @Test
    fun `valid MSO decodes null expectedUpdate when absent`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.validityInfo.expectedUpdate, equalTo(null))
    }

    @Test
    fun `decode throws MALFORMED_MSO for invalid expectedUpdate timestamp`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoWithInvalidExpectedUpdate)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_MSO))
    }

    @Test
    fun `valid MSO decodes multiple namespaces in valueDigests`() {
        val mso = decoder.decode(encodedMsoWithMultipleNamespaces)
        assertThat(mso.valueDigests.keys.size, equalTo(2))
        assertNotNull(mso.valueDigests["org.iso.18013.5.1"])
        assertNotNull(mso.valueDigests["org.iso.18013.5.1.aamva"])
    }

    @Test
    fun `valid MSO decodes status field when present`() {
        val mso = decoder.decode(encodedMsoWithStatus)
        assertNotNull(mso.status)
    }

    @Test
    fun `valid MSO decodes null status when absent`() {
        val mso = decoder.decode(validEncodedMSO)
        assertThat(mso.status, equalTo(null))
    }

    @Test
    fun `decode throws INVALID_MSO_VERSION for unsupported version`() {
        val encoded = MobileSecurityObjectStubs.wrapTag24(
            MobileSecurityObjectStubs.buildMsoBytes(version = "2.0")
        )
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encoded)
        }
        assertThat(exception, hasError(VerificationError.INVALID_MSO_VERSION))
    }

    @Test
    fun `decode throws UNSUPPORTED_DIGEST_ALGORITHM for non-SHA-256`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            decoder.decode(encodedMsoWithUnsupportedAlgorithm)
        }
        assertThat(exception, hasError(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM))
    }
}
