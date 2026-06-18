package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.time.ExperimentalTime
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocument
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemStubs.issuerSignedItemBytes
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

@OptIn(ExperimentalTime::class)
@RunWith(TestParameterInjector::class)
class VerifyMsoFieldsUseCaseImplTest {

    private val useCase = VerifyMsoFieldsUseCaseImpl()
    private val validityPeriod = mockk<CertificateValidityPeriod>(relaxed = true)

    private val defaultIssuerAuthResult = IssuerAuthResult(
        certificateValidityPeriod = validityPeriod,
        msoPayload = byteArrayOf(),
        subjectCountry = "GB",
        subjectState = "London"
    )

    @Test
    fun `verify does not throw for valid fields`() {
        useCase.verify(validDocument(), validMso(), defaultIssuerAuthResult)
    }

    @Test
    fun `verify does not throw when issuing_country matches leaf certificate`() {
        val document = documentWithNameSpaces(
            mapOf(
                MobileSecurityObject.NAMESPACE to listOf(
                    issuerSignedItemBytes("issuing_country", "GB")
                )
            )
        )
        useCase.verify(document, validMso(), defaultIssuerAuthResult)
    }

    @Test
    fun `verify accepts version with non-zero minor`(
        @TestParameter version: String = testValues("1.1", "1.99")
    ) {
        useCase.verify(validDocument(), validMso(version = version), defaultIssuerAuthResult)
    }

    @Test
    fun `verify throws INVALID_MSO_VERSION for non-1 major`(
        @TestParameter version: String = testValues("2.0", "0.1", "3.5")
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(validDocument(), validMso(version = version), defaultIssuerAuthResult)
        }
        assertThat(exception, hasError(VerificationError.INVALID_MSO_VERSION))
    }

    @Test
    fun `verify throws INVALID_DOC_TYPE when mso docType is not mDL`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(
                validDocument(),
                validMso(docType = "some.other.type"),
                defaultIssuerAuthResult
            )
        }
        assertThat(exception, hasError(VerificationError.INVALID_DOC_TYPE))
    }

    @Test
    fun `verify throws INVALID_DOC_TYPE when mso docType does not match document`() {
        val document = SharingVerifiableDocument(
            docType = "different",
            issuerSigned = SharingIssuerSigned(issuerAuth = byteArrayOf(), nameSpaces = null)
        )
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(document, validMso(), defaultIssuerAuthResult)
        }
        assertThat(exception, hasError(VerificationError.INVALID_DOC_TYPE))
    }

    @Test
    fun `verify throws UNSUPPORTED_DIGEST_ALGORITHM for non-SHA-256`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(
                validDocument(),
                validMso(digestAlgorithm = "SHA-512"),
                defaultIssuerAuthResult
            )
        }
        assertThat(exception, hasError(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM))
    }

    @Test
    fun `verify throws INVALID_MSO for issuing_country mismatch`() {
        val document = documentWithNameSpaces(
            mapOf(
                MobileSecurityObject.NAMESPACE to listOf(
                    issuerSignedItemBytes("issuing_country", "US")
                )
            )
        )
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(document, validMso(), defaultIssuerAuthResult)
        }
        assertThat(exception, hasError(VerificationError.INVALID_MSO))
    }

    @Test
    fun `verify does not throw when jurisdiction present but leaf has no state`() {
        val document = documentWithNameSpaces(
            mapOf(
                MobileSecurityObject.NAMESPACE to listOf(
                    issuerSignedItemBytes("issuing_jurisdiction", "CA")
                )
            )
        )
        val issuerAuth = defaultIssuerAuthResult.copy(subjectState = null)
        useCase.verify(document, validMso(), issuerAuth)
    }

    @Test
    fun `verify throws INVALID_MSO for issuing_jurisdiction mismatch`() {
        val document = documentWithNameSpaces(
            mapOf(
                MobileSecurityObject.NAMESPACE to listOf(
                    issuerSignedItemBytes("issuing_jurisdiction", "CA")
                )
            )
        )
        val issuerAuth = defaultIssuerAuthResult.copy(subjectState = "NY")
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            useCase.verify(document, validMso(), issuerAuth)
        }
        assertThat(exception, hasError(VerificationError.INVALID_MSO))
    }

    private fun validMso(
        version: String = "1.0",
        docType: String = MobileSecurityObject.DOC_TYPE,
        digestAlgorithm: String = MobileSecurityObject.MSO_DIGEST_ALGORITHM
    ) = MobileSecurityObject(
        version = version,
        docType = docType,
        digestAlgorithm = digestAlgorithm,
        valueDigests = emptyMap(),
        deviceKeyInfo = DeviceKeyInfo(deviceKey = byteArrayOf()),
        validityInfo = mockk()
    )

    private fun validDocument(): VerifiableDocument = SharingVerifiableDocument(
        docType = MobileSecurityObject.DOC_TYPE,
        issuerSigned = SharingIssuerSigned(issuerAuth = byteArrayOf(), nameSpaces = null)
    )

    private fun documentWithNameSpaces(
        nameSpaces: Map<String, List<ByteArray>>
    ): VerifiableDocument = SharingVerifiableDocument(
        docType = MobileSecurityObject.DOC_TYPE,
        issuerSigned = SharingIssuerSigned(issuerAuth = byteArrayOf(), nameSpaces = nameSpaces)
    )
}
