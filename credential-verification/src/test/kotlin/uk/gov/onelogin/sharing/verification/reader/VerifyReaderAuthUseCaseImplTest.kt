package uk.gov.onelogin.sharing.verification.reader

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.cert.X509Certificate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ReaderAuthenticationDto
import uk.gov.onelogin.sharing.verification.cose.CertificateProfileReason
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationRequest
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationResult
import uk.gov.onelogin.sharing.verification.cose.CoseVerifier

@RunWith(TestParameterInjector::class)
class VerifyReaderAuthUseCaseImplTest {

    private val coseVerifier: CoseVerifier = mockk()
    private lateinit var useCase: VerifyReaderAuthUseCaseImpl

    private val sampleTranscript = byteArrayOf(0x01, 0x02, 0x03)
    private val sampleItemsRequestBytes = byteArrayOf(0xD8.toByte(), 0x18.toByte(), 0x04)
    private val sampleRawReaderAuth = byteArrayOf(0x84.toByte(), 0x01, 0x02, 0x03)
    private val mockCert: X509Certificate = mockk()
    private val sampleDocRequest = DocRequest(
        itemsRequest = ItemsRequest(docType = "org.iso.18013.5.1.mDL", nameSpaces = emptyMap()),
        readerAuth = sampleRawReaderAuth,
        itemsRequestBytes = sampleItemsRequestBytes
    )

    @Before
    fun setUp() {
        useCase = VerifyReaderAuthUseCaseImpl(coseVerifier)
    }

    @Test
    fun `valid ReaderAuth verifies signature and returns VerifiedReaderRequest`() {
        val expectedPayload =
            ReaderAuthenticationDto.createReaderAuthenticationBytes(
                sampleTranscript,
                sampleItemsRequestBytes
            )
        val expectedResult = CoseVerificationResult.Detached(mockCert)

        every {
            coseVerifier.verify(
                CoseVerificationRequest.Detached(
                    coseSign1Bytes = sampleRawReaderAuth,
                    detachedPayload = expectedPayload,
                    trustedRoots = listOf(mockCert)
                )
            )
        } returns expectedResult

        val result = useCase.verify(
            candidateDocRequest = sampleDocRequest,
            untaggedSessionTranscriptBytes = sampleTranscript,
            trustedReaderCertificates = listOf(mockCert)
        )

        assertEquals(sampleDocRequest, result.docRequest)
        assertEquals(mockCert, result.readerCertificate)

        verify(exactly = 1) {
            coseVerifier.verify(
                CoseVerificationRequest.Detached(
                    coseSign1Bytes = sampleRawReaderAuth,
                    detachedPayload = expectedPayload,
                    trustedRoots = listOf(mockCert)
                )
            )
        }
    }

    @Test
    fun `missing readerAuth throws READER_AUTH_MISSING without calling coseVerifier`() {
        val docRequestWithoutAuth = sampleDocRequest.copy(readerAuth = null)

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.verify(
                candidateDocRequest = docRequestWithoutAuth,
                untaggedSessionTranscriptBytes = sampleTranscript,
                trustedReaderCertificates = listOf(mockCert)
            )
        }

        assertEquals(ReaderAuthenticationReason.READER_AUTH_MISSING, failure.reason)
        verify(exactly = 0) { coseVerifier.verify(any()) }
    }

    @Test
    fun `missing itemsRequestBytes throws MALFORMED_READER_AUTH without calling coseVerifier`() {
        val docRequestWithoutItems = sampleDocRequest.copy(itemsRequestBytes = null)

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.verify(
                candidateDocRequest = docRequestWithoutItems,
                untaggedSessionTranscriptBytes = sampleTranscript,
                trustedReaderCertificates = listOf(mockCert)
            )
        }

        assertEquals(ReaderAuthenticationReason.MALFORMED_READER_AUTH, failure.reason)
        verify(exactly = 0) { coseVerifier.verify(any()) }
    }

    data class CoseFailureMappingCase(
        val failure: CoseVerificationFailure,
        val expectedReason: ReaderAuthenticationReason
    )

    @Test
    fun `maps CoseVerificationFailure to corresponding ReaderAuthenticationReason`(
        @TestParameter case: CoseFailureMappingCase = namedTestValues(
            "InvalidSignature" to CoseFailureMappingCase(
                CoseVerificationFailure.InvalidSignature,
                ReaderAuthenticationReason.INVALID_READER_SIGNATURE
            ),
            "MalformedCoseSign1" to CoseFailureMappingCase(
                CoseVerificationFailure.MalformedCoseSign1,
                ReaderAuthenticationReason.MALFORMED_READER_AUTH
            ),
            "MissingX5Chain" to CoseFailureMappingCase(
                CoseVerificationFailure.MissingX5Chain,
                ReaderAuthenticationReason.MALFORMED_READER_AUTH
            ),
            "UnsupportedAlgorithm" to CoseFailureMappingCase(
                CoseVerificationFailure.UnsupportedAlgorithm,
                ReaderAuthenticationReason.UNSUPPORTED_READER_AUTH_ALGORITHM
            ),
            "UntrustedCertificate" to CoseFailureMappingCase(
                CoseVerificationFailure.UntrustedCertificate(),
                ReaderAuthenticationReason.UNTRUSTED_READER_CERTIFICATE
            ),
            "ExpiredCertificate" to CoseFailureMappingCase(
                CoseVerificationFailure.ExpiredCertificate,
                ReaderAuthenticationReason.UNTRUSTED_READER_CERTIFICATE
            ),
            "CertificateProfileViolation" to CoseFailureMappingCase(
                CoseVerificationFailure.CertificateProfileViolation(
                    CertificateProfileReason.INVALID_KEY_USAGE
                ),
                ReaderAuthenticationReason.UNTRUSTED_READER_CERTIFICATE
            )
        )
    ) {
        every { coseVerifier.verify(any()) } throws case.failure

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            useCase.verify(
                candidateDocRequest = sampleDocRequest,
                untaggedSessionTranscriptBytes = sampleTranscript,
                trustedReaderCertificates = listOf(mockCert)
            )
        }

        assertEquals(case.expectedReason, failure.reason)
    }
}
