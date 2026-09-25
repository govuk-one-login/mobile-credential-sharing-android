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
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class ReaderAuthenticationImplTest {

    private val verifyReaderAuthUseCase: VerifyReaderAuthUseCase = mockk()
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase = mockk()
    private lateinit var readerAuthentication: ReaderAuthenticationImpl

    private val mockCert: X509Certificate = mockk()
    private val sampleTranscript = byteArrayOf(0x01, 0x02)
    private val docTypeMdl = "org.iso.18013.5.1.mDL"
    private val docTypeAamva = "org.iso.18013.5.1.aamva"
    private val docTypeEvrc = "org.iso.18013.5.1.eVRC"
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
    fun `empty docRequests returns Unfulfillable`() {
        val emptyDeviceRequest = DeviceRequest(version = "1.0", docRequests = emptyList())

        val outcome = readerAuthentication.authenticateDeviceRequest(
            deviceRequest = emptyDeviceRequest,
            untaggedSessionTranscriptBytes = sampleTranscript,
            supportedDocumentTypes = supportedTypes
        )

        assertTrue(outcome is ReaderAuthenticationOutcome.Unfulfillable)
        verify(exactly = 0) { verifyReaderAuthUseCase.verify(any(), any(), any()) }
        verify(exactly = 0) { validatePrivacyPolicyUseCase.validate(any()) }
    }

    @Test
    fun `request with no supported docTypes returns Unfulfillable without calling R4 or R5`() {
        val unsupportedDocRequest = DocRequest(itemsRequest = ItemsRequest(docType = docTypeEvrc, nameSpaces = sampleNameSpaces))
        val deviceRequest = DeviceRequest(version = "1.0", docRequests = listOf(unsupportedDocRequest))

        val outcome = readerAuthentication.authenticateDeviceRequest(
            deviceRequest = deviceRequest,
            untaggedSessionTranscriptBytes = sampleTranscript,
            supportedDocumentTypes = supportedTypes
        )

        assertTrue(outcome is ReaderAuthenticationOutcome.Unfulfillable)
        verify(exactly = 0) { verifyReaderAuthUseCase.verify(any(), any(), any()) }
        verify(exactly = 0) { validatePrivacyPolicyUseCase.validate(any()) }
    }

    @Test
    fun `skips unsupported candidate eVRC and authenticates first supported candidate mDL`() {
        val mockUri: Uri = mockk()
        val docReqEvrc = DocRequest(itemsRequest = ItemsRequest(docType = docTypeEvrc, nameSpaces = sampleNameSpaces))
        val docReqMdl = DocRequest(itemsRequest = ItemsRequest(docType = docTypeMdl, nameSpaces = sampleNameSpaces))

        val deviceRequest = DeviceRequest(version = "1.0", docRequests = listOf(docReqEvrc, docReqMdl))
        val verifiedReqMdl = VerifiedReaderRequest(docReqMdl, mockCert)

        every {
            verifyReaderAuthUseCase.verify(docReqMdl, any(), any())
        } returns verifiedReqMdl

        every {
            validatePrivacyPolicyUseCase.validate(verifiedReqMdl)
        } returns AuthenticatedReaderRequest(
            docRequest = docReqMdl,
            privacyPolicyUrl = mockUri,
            readerOrganizationName = "GOV.UK OneLogin"
        )

        val outcome = readerAuthentication.authenticateDeviceRequest(
            deviceRequest = deviceRequest,
            untaggedSessionTranscriptBytes = sampleTranscript,
            supportedDocumentTypes = listOf(docTypeMdl)
        )

        assertTrue(outcome is ReaderAuthenticationOutcome.Success)
        val success = outcome as ReaderAuthenticationOutcome.Success
        assertEquals(docTypeMdl, success.authenticatedReaderRequest.docRequest.itemsRequest.docType)

        // eVRC is skipped completely (0 calls for eVRC)
        verify(exactly = 0) { verifyReaderAuthUseCase.verify(docReqEvrc, any(), any()) }
        verify(exactly = 1) { verifyReaderAuthUseCase.verify(docReqMdl, any(), any()) }
    }

    @Test
    fun `selects first candidate that passes both R4 and R5`() {
        val mockUri: Uri = mockk()
        val docReqA = DocRequest(itemsRequest = ItemsRequest(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqB = DocRequest(itemsRequest = ItemsRequest(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqC = DocRequest(itemsRequest = ItemsRequest(docType = docTypeAamva, nameSpaces = sampleNameSpaces))
        val docReqD = DocRequest(itemsRequest = ItemsRequest(docType = docTypeAamva, nameSpaces = sampleNameSpaces))

        val deviceRequest = DeviceRequest(version = "1.0", docRequests = listOf(docReqA, docReqB, docReqC, docReqD))

        val verifiedReqB = VerifiedReaderRequest(docReqB, mockCert)
        val verifiedReqC = VerifiedReaderRequest(docReqC, mockCert)

        // Candidate A fails R4, Candidate B passes R4, Candidate C passes R4
        every {
            verifyReaderAuthUseCase.verify(any(), any(), any())
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.INVALID_READER_SIGNATURE) andThen
            verifiedReqB andThen
            verifiedReqC

        // Candidate B fails R5
        every {
            validatePrivacyPolicyUseCase.validate(verifiedReqB)
        } throws ReaderAuthenticationFailure(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID)

        // Candidate C passes R5
        every {
            validatePrivacyPolicyUseCase.validate(verifiedReqC)
        } returns AuthenticatedReaderRequest(
            docRequest = docReqC,
            privacyPolicyUrl = mockUri,
            readerOrganizationName = "GOV.UK OneLogin"
        )

        val outcome = readerAuthentication.authenticateDeviceRequest(
            deviceRequest = deviceRequest,
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
    fun `all candidates fail throws final candidate failure`() {
        val docReqA = DocRequest(itemsRequest = ItemsRequest(docType = docTypeMdl, nameSpaces = sampleNameSpaces))
        val docReqB = DocRequest(itemsRequest = ItemsRequest(docType = docTypeAamva, nameSpaces = sampleNameSpaces))

        val deviceRequest = DeviceRequest(version = "1.0", docRequests = listOf(docReqA, docReqB))

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
                deviceRequest = deviceRequest,
                untaggedSessionTranscriptBytes = sampleTranscript,
                supportedDocumentTypes = supportedTypes
            )
        }

        assertEquals(ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID, failure.reason)
    }
}
