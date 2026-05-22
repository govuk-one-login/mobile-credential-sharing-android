package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.cert.X509Certificate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.document.models.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.document.models.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@RunWith(TestParameterInjector::class)
class Iso18013DocumentVerifierTest {
    private val classInfo = scanResult.getClassInfo(Iso18013DocumentVerifier::class.java.name)
    private val privateFunctionSuffix = $$"$credential_verification"

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
            equalTo("void (" +
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
        )
    ) {
        val (name, expectedDescriptor) = functionsToDescriptors
        val methodInfo = classInfo.methodInfo.getSingleMethod("$name$privateFunctionSuffix")

        assertThat(
            methodInfo.typeDescriptor.toStringWithSimpleNames(),
            equalTo(expectedDescriptor)
        )
    }
}