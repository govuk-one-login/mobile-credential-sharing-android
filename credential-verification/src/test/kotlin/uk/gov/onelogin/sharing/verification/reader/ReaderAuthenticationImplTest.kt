package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.security.cert.X509Certificate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto

class ReaderAuthenticationImplTest {

    private val verifyReaderAuthUseCase: VerifyReaderAuthUseCase = mockk()
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase = mockk()
    private lateinit var readerAuthentication: ReaderAuthenticationImpl

    private val mockCert: X509Certificate = mockk()
    private val sampleTranscript = byteArrayOf(0x01, 0x02)
    private val docTypeMdl = "org.iso.18013.5.1.mDL"
    private val docTypeAamva = "org.iso.18013.5.1.aamva"
    private val supportedTypes = listOf(docTypeMdl, docTypeAamva)
    private val sampleNameSpaces = mapOf("org.iso.18013.5.1" to mapOf("family_name" to false))

    @Before
    fun setUp() {
        readerAuthentication = ReaderAuthenticationImpl(
            verifyReaderAuthUseCase = verifyReaderAuthUseCase,
            validatePrivacyPolicyUseCase = validatePrivacyPolicyUseCase,
            trustedReaderCertificates = listOf(mockCert),
        )
    }

    @Test
    fun `AC1 - invalid deviceRequest CBOR bytes throws MALFORMED_DEVICE_REQUEST`() {
        val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte())

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            readerAuthentication.authenticateDeviceRequest(
                decryptedDeviceRequestBytes = invalidBytes,
                untaggedSessionTranscriptBytes = sampleTranscript,
                supportedDocumentTypes = supportedTypes
            )
        }

        assertEquals(ReaderAuthenticationReason.MALFORMED_DEVICE_REQUEST, failure.reason)
    }

    @Test
    fun `AC2 - request with unsupported docType returns Unfulfillable without calling R4 or R5`() {
        val unsupportedDto = DeviceRequestDto(
            version = "1.0",
            docRequest = listOf(
                DocRequestDto(itemsRequest = ItemsRequestDto(docType = "unsupported.doc.type", nameSpaces = sampleNameSpaces))
            )
        )
        val bytes = CborMapper.default.writeValueAsBytes(unsupportedDto)

        val outcome = readerAuthentication.authenticateDeviceRequest(
            decryptedDeviceRequestBytes = bytes,
            untaggedSessionTranscriptBytes = sampleTranscript,
            supportedDocumentTypes = supportedTypes
        )

        assertTrue(outcome is ReaderAuthenticationOutcome.Unfulfillable)
        verify(exactly = 0) { verifyReaderAuthUseCase.verify(any(), any(), any()) }
        verify(exactly = 0) { validatePrivacyPolicyUseCase.validate(any()) }
    }

    @Test
    fun `AC3 - selects first candidate that passes both R4 and R5`() {
        val mockUri: Uri = mockk()
        val docReqDtoA = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqDtoB = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqDtoC = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeAamva, nameSpaces = sampleNameSpaces))
        val docReqDtoD = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeAamva, nameSpaces = sampleNameSpaces))

        val dto = DeviceRequestDto(
            version = "1.0",
            docRequest = listOf(docReqDtoA, docReqDtoB, docReqDtoC, docReqDtoD)
        )
        val bytes = CborMapper.default.writeValueAsBytes(dto)

        val sampleMdlDocRequest = DocRequest(itemsRequest = ItemsRequest(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val sampleAamvaDocRequest = DocRequest(itemsRequest = ItemsRequest(docType = docTypeAamva, nameSpaces = sampleNameSpaces))

        val verifiedReqB = VerifiedReaderRequest(sampleMdlDocRequest, mockCert)
        val verifiedReqC = VerifiedReaderRequest(sampleAamvaDocRequest, mockCert)

        // Candidate A (mDL) fails R4, Candidate B (mDL) passes R4, Candidate C (aamva) passes R4
        every {
            verifyReaderAuthUseCase.verify(any(), any(), any())
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.INVALID_READER_SIGNATURE) andThen
            verifiedReqB andThen
            verifiedReqC

        // Candidate B (mDL) fails R5
        every {
            validatePrivacyPolicyUseCase.validate(verifiedReqB)
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID)

        // Candidate C (aamva) passes R5
        every {
            validatePrivacyPolicyUseCase.validate(verifiedReqC)
        } returns AuthenticatedReaderRequest(
            docRequest = sampleAamvaDocRequest,
            privacyPolicyUrl = mockUri,
            readerOrganizationName = "GOV.UK OneLogin"
        )

        val outcome = readerAuthentication.authenticateDeviceRequest(
            decryptedDeviceRequestBytes = bytes,
            untaggedSessionTranscriptBytes = sampleTranscript,
            supportedDocumentTypes = supportedTypes
        )

        assertTrue(outcome is ReaderAuthenticationOutcome.Success)
        val success = outcome as ReaderAuthenticationOutcome.Success
        assertEquals(mockUri, success.authenticatedReaderRequest.privacyPolicyUrl)
        assertEquals("GOV.UK OneLogin", success.authenticatedReaderRequest.readerOrganizationName)

        // Candidate D is never evaluated (short-circuit)
        verify(exactly = 3) { verifyReaderAuthUseCase.verify(any(), any(), any()) }
    }

    @Test
    fun `AC4 - all candidates fail throws final candidate failure`() {
        val docReqDtoA = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqDtoB = DocRequestDto(itemsRequest = ItemsRequestDto(docType = docTypeAamva, nameSpaces = sampleNameSpaces))

        val dto = DeviceRequestDto(version = "1.0", docRequest = listOf(docReqDtoA, docReqDtoB))
        val bytes = CborMapper.default.writeValueAsBytes(dto)

        // Candidate A fails R4 with INVALID_READER_SIGNATURE
        every {
            verifyReaderAuthUseCase.verify(match { it.itemsRequest.docType == docTypeMdl }, any(), any())
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.INVALID_READER_SIGNATURE)

        // Candidate B passes R4
        every {
            verifyReaderAuthUseCase.verify(match { it.itemsRequest.docType == docTypeAamva }, any(), any())
        } returns VerifiedReaderRequest(mockk(), mockCert)

        // Candidate B fails R5 with PRIVACY_POLICY_URL_INVALID
        every {
            validatePrivacyPolicyUseCase.validate(any())
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID)

        val failure = assertThrows(ReaderAuthenticationFailure::class.java) {
            readerAuthentication.authenticateDeviceRequest(
                decryptedDeviceRequestBytes = bytes,
                untaggedSessionTranscriptBytes = sampleTranscript,
                supportedDocumentTypes = supportedTypes
            )
        }

        assertEquals(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID, failure.reason)
    }
}
