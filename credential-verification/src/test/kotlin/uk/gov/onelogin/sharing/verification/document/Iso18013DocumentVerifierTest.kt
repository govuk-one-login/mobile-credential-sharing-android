package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.security.cert.X509Certificate
import kotlin.time.ExperimentalTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Assert.assertThrows
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocument
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.format.document.IssuerSignedStubs.validIssuerAuth
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithInvalidVersion
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.encodedMsoWithMismatchedDigests
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.malformedEncodedMSO
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObjectStubs.validEncodedMSO
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriodStubs
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@OptIn(ExperimentalTime::class)
@RunWith(TestParameterInjector::class)
class Iso18013DocumentVerifierTest {
    private val classInfo = scanResult.getClassInfo(Iso18013DocumentVerifier::class.java.name)
    private val privateFunctionSuffix = $$"$credential_verification"

    /**
     * DCMAW-20269: AC1: A Sharing SDK document can be wrapped in [SharingVerifiableDocument] and
     * passed directly to [Iso18013DocumentVerifier.verifyDocument].
     */
    private val mockProvisionedDocument: VerifiableDocument = SharingVerifiableDocument(
        docType = "unit test",
        issuerSigned = SharingIssuerSigned(
            issuerAuth = validIssuerAuth,
            nameSpaces = null
        )
    )
    private val mockPresentedDocument: VerifiableDocument.WithPresentation = mockk(relaxed = true)
    private val sessionTranscriptBytes: ByteArray = byteArrayOf(1, 2)
    private val mockRootCertificate: X509Certificate = mockk(relaxed = true)
    private val trustVerifier: TrustVerifier = mockk(relaxed = true)
    private val validityPeriod: CertificateValidityPeriod = mockk(relaxed = true)

    private val documentVerifier by lazy {
        Iso18013DocumentVerifier(
            mockRootCertificate,
            trustVerifier
        )
    }

    /**
     * DCMAW-20246: AC1: [Iso18013DocumentVerifier] compiles, implements [DocumentVerifier],
     * accepts a [X509Certificate] and a [TrustVerifier] in its constructor.
     */
    @Test
    fun `Ensure constructor constraints`() {
        val constructorInfo = classInfo.constructorInfo

        assertThat(
            constructorInfo,
            hasSize(1)
        )

        assertThat(
            constructorInfo[0].typeDescriptor.toStringWithSimpleNames(),
            equalTo(
                "void (" +
                    "${X509Certificate::class.java.simpleName}, " +
                    "${TrustVerifier::class.java.simpleName})"
            )
        )
    }

    /**
     * DCMAW-20246: AC3: 'Private' methods exist on [Iso18013DocumentVerifier] with the correct
     * signatures
     */
    @Test
    fun `Ensure function count`() {
        val methodInfo = classInfo.methodInfo

        assertThat(
            methodInfo,
            hasSize(7)
        )
    }

    /**
     * DCMAW-20246: AC3: 'Private' methods exist on [Iso18013DocumentVerifier] with the correct
     * signatures
     */
    @Test
    fun `Ensure private function signature`(
        @TestParameter functionsToDescriptors: Pair<String, String> = testValues(
            "decodeMSO" to "${MobileSecurityObject::class.java.name} (byte[])",
            "verifyMSOFields" to "void (" +
                "${VerifiableDocument::class.java.simpleName}, " +
                "${MobileSecurityObject::class.java.simpleName}" +
                ")",
            "verifyDocumentDigests" to
                "void (" +
                "${VerifiableDocument::class.java.simpleName}, " +
                "${MobileSecurityObject::class.java.simpleName}" +
                ")",
            "verifyValidityInfo" to
                "void (" +
                "${CertificateValidityPeriod::class.java.simpleName}, " +
                "${MobileSecurityObject::class.java.simpleName}" +
                ")",
            "verifyDeviceAuth" to
                "void (" +
                "${VerifiableDocument.WithPresentation::class.java.simpleName}, " +
                "byte[], " +
                "${DeviceKeyInfo::class.java.simpleName}" +
                ")",
            "buildDeviceAuthenticationBytes" to
                "${ByteArray::class.java.simpleName} (" +
                "byte[], " +
                "${String::class.java.simpleName}, " +
                "${ByteArray::class.java.simpleName}" +
                ")"
        )
    ) {
        val (name, expectedDescriptor) = functionsToDescriptors
        val methodInfo = classInfo.methodInfo.getSingleMethod("$name$privateFunctionSuffix")

        assertThat(
            methodInfo.typeDescriptor.toStringWithSimpleNames(),
            equalTo(expectedDescriptor)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] calls [TrustVerifier.verifyCOSESign1]
     * first.
     */
    @Test
    fun `Fails verification due to TrustVerifier failing COSE sign`(
        @TestParameter error: VerificationError = testValues(
            VerificationError.MALFORMED_ISSUER_AUTH,
            VerificationError.INVALID_ISSUER_SIGNATURE,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    ) {
        every {
            trustVerifier.verifyCOSESign1(
                eq(validIssuerAuth),
                eq(mockRootCertificate)
            )
        } throws VerificationResult.Failure(error)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockProvisionedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(error)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] calls
     * [Iso18013DocumentVerifier.decodeMSO] after verifying trust.
     */
    @Test
    fun `Fails verification due to MSO decoding error`() {
        stubTrustVerifierSuccess(encodedMSO = malformedEncodedMSO)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockProvisionedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.MALFORMED_MSO)
        )
    }

    @Test
    fun `decodeMSO is stubbed to throw a Failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.decodeMSO(validEncodedMSO)
        }

        assertThat(
            exception,
            hasError(VerificationError.MALFORMED_MSO)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] verifies [MobileSecurityObject] fields.
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Fails verification due to invalid MSO fields`() {
        stubTrustVerifierSuccess(encodedMSO = encodedMsoWithInvalidVersion)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockProvisionedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_MSO_VERSION)
        )
    }

    @Test
    fun `verifyMSOFields is stubbed to throw a Failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyMSOFields(mockProvisionedDocument, mockk())
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_MSO_VERSION)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] verifies [MobileSecurityObject]
     * value digests.
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Fails verification due to invalid Document Digests`() {
        stubTrustVerifierSuccess(encodedMSO = encodedMsoWithMismatchedDigests)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockProvisionedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.DIGEST_MISMATCH)
        )
    }

    @Test
    fun `verifyDocumentDigests is stubbed to throw a Failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocumentDigests(
                document = mockProvisionedDocument,
                mso = mockk()
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.DIGEST_MISMATCH)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] verifies [CertificateValidityPeriod]
     * against the [MobileSecurityObject].
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Fails verification due to expired validity info`() {
        stubTrustVerifierSuccess(
            validityPeriod = CertificateValidityPeriodStubs.expired()
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockProvisionedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)
        )
    }

    @Test
    fun `verifyValidityInfo is stubbed to throw a Failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyValidityInfo(
                validityPeriod = CertificateValidityPeriodStubs.expired(),
                mso = mockk()
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)
        )
    }

    /**
     * DCMAW-20246: AC4: [DocumentVerifier.verifyDocument] verifies device authentication
     * with [VerifiableDocument.WithPresentation].
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Fails verification due to presented document failing device auth`() {
        stubTrustVerifierSuccess()

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockPresentedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_DEVICE_SIGNATURE)
        )
    }

    @Test
    fun `verifyDeviceAuth is stubbed to throw a Failure`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDeviceAuth(
                document = mockPresentedDocument,
                sessionTranscriptBytes = sessionTranscriptBytes,
                deviceKeyInfo = mockk()
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_DEVICE_SIGNATURE)
        )
    }

    /**
     * DCMAW-20246: AC5: If document does not conform to [VerifiableDocument.WithPresentation],
     * [Iso18013DocumentVerifier.verifyDeviceAuth] is not called regardless of sessionTranscript.
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Doesn't perform device auth verification on Provisioned documents`() {
        stubTrustVerifierSuccess()
        val documentVerifierSpy = spyk(documentVerifier)

        documentVerifierSpy.verifyDocument(
            document = mockProvisionedDocument,
            sessionTranscriptBytes = sessionTranscriptBytes
        )

        verify(exactly = 0) {
            documentVerifierSpy.verifyDeviceAuth(any(), any(), any())
        }
    }

    /**
     * DCMAW-20246: AC6: If document conforms to [VerifiableDocument.WithPresentation] but
     * sessionTranscript is null, [DocumentVerifier.verifyDocument] fails with
     * [VerificationError.INVALID_DEVICE_SIGNATURE] - DeviceAuth is mandatory for presentation
     * documents and cannot be silently skipped.
     */
    @Ignore("Currently untestable via interface functions")
    @Test
    fun `Fails verification due to null transcript with a presented Document`() {
        stubTrustVerifierSuccess()

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockPresentedDocument,
                sessionTranscriptBytes = null
            )
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_DEVICE_SIGNATURE)
        )
    }

    @Test
    fun `buildDeviceAuthenticationBytes is stubbed to return an empty ByteArray`() {
        val deviceAuthBytes = documentVerifier.buildDeviceAuthenticationBytes(
            sessionTranscriptBytes,
            "unit test",
            byteArrayOf()
        )

        assertThat(
            deviceAuthBytes,
            equalTo(byteArrayOf())
        )
    }

    private fun stubTrustVerifierSuccess(
        encodedMSO: ByteArray = validEncodedMSO,
        validityPeriod: CertificateValidityPeriod = this.validityPeriod
    ) {
        every {
            trustVerifier.verifyCOSESign1(any(), any())
        } returns Pair(validityPeriod, encodedMSO)
    }
}
