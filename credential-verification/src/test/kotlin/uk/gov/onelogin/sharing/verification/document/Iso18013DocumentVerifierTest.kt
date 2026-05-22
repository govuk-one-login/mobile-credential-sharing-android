package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.document.models.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.document.models.IssuerSigned
import uk.gov.onelogin.sharing.verification.document.models.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@RunWith(TestParameterInjector::class)
class Iso18013DocumentVerifierTest {
    private val classInfo = scanResult.getClassInfo(Iso18013DocumentVerifier::class.java.name)
    private val privateFunctionSuffix = $$"$credential_verification"

    private val mockDocument: VerifiableDocument = mockk()
    private val issuerSigned: IssuerSigned = mockk()
    private val issuerAuth: ByteArray = byteArrayOf()
    private val mockTranscript: SessionTranscript = mockk()
    private val mockRootCertificate: X509Certificate = mockk()
    private val trustVerifier: TrustVerifier = mockk()

    private val documentVerifier by lazy {
        Iso18013DocumentVerifier(
            mockRootCertificate,
            trustVerifier
        )
    }

    @Before
    fun setUp() {
        every {
            mockDocument.issuerSigned
        } returns issuerSigned
        every {
            issuerSigned.issuerAuth
        } returns issuerAuth
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
                    "${SessionTranscript::class.java.simpleName}, " +
                    "${DeviceKeyInfo::class.java.simpleName}" +
                    ")",
            "buildDeviceAuthenticationBytes" to
                    "${ByteArray::class.java.simpleName} (" +
                    "${SessionTranscript::class.java.simpleName}, " +
                    "${String::class.java.simpleName}, " +
                    "${ByteArray::class.java.simpleName}" +
                    ")"
        ),
    ) {
        val (name, expectedDescriptor) = functionsToDescriptors
        val methodInfo = classInfo.methodInfo.getSingleMethod("$name$privateFunctionSuffix")

        assertThat(
            methodInfo.typeDescriptor.toStringWithSimpleNames(),
            equalTo(expectedDescriptor)
        )
    }

    /**
     * DCMAW-20246: AC4: verifyDocument() calls [TrustVerifier.verifyCOSESign1] first,
     * then [Iso18013DocumentVerifier.decodeMSO], then the remaining three validators in order,
     * then [Iso18013DocumentVerifier.verifyDeviceAuth] conditionally - each step fails the call
     * if it throws.
     */
    @Test
    fun `Fails verification due to TrustVerifier failing COSE sign`(
        @TestParameter error: VerificationError = testValues(
            VerificationError.MALFORMED_ISSUER_AUTH,
            VerificationError.INVALID_ISSUER_SIGNATURE,
            VerificationError.UNTRUSTED_CERTIFICATE,
        ),
    ) {
        every {
            trustVerifier.verifyCOSESign1(
                eq(issuerAuth),
                eq(mockRootCertificate)
            )
        } throws VerificationResult.Failure(error)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            documentVerifier.verifyDocument(
                document = mockDocument,
                transcript = mockTranscript
            )
        }

        assertThat(
            exception,
            hasError(error)
        )
    }
}