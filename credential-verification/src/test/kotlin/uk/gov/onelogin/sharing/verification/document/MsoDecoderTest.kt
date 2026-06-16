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
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DIGEST_ALGORITHM
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_DOC_TYPE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_NAMESPACE
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.DEFAULT_VERSION
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithDuplicateKeys
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithFractionalSeconds
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithNumericOffset
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
        assertThat(mso.validityInfo.signed, equalTo(Instant.parse("2024-01-15T10:00:00Z")))
        assertThat(mso.validityInfo.validFrom, equalTo(Instant.parse("2024-01-15T10:00:00Z")))
        assertThat(mso.validityInfo.validUntil, equalTo(Instant.parse("2025-01-15T10:00:00Z")))
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
}
