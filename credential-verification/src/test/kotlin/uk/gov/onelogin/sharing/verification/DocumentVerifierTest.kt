package uk.gov.onelogin.sharing.verification

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.result.VerificationError
import uk.gov.onelogin.sharing.verification.result.VerificationResult

@RunWith(TestParameterInjector::class)
class DocumentVerifierTest {
    /**
     * DCMAW-20245: AC8: [DocumentVerifier] exposes `verifyDocument(document, sessionTranscript?)`
     * returning [uk.gov.onelogin.sharing.verification.result.VerificationResult.Success].
     */
    @Test
    fun `Ensure DocumentVerifier constraints`() {
        val expectedMethods = listOf(
            "verifyDocument" to VerificationResult.Success::class.java
        )
        val expectedMethodParameters = listOf(
            VerifiableDocument::class,
            SessionTranscript::class
        ).map { it.java.name }

        val classInfo = scanResult.getClassInfo(DocumentVerifier::class.java.name)

        assertInterfaceReturnTypes(expectedMethods, classInfo)

        val methodParameters = classInfo.methodInfo.getSingleMethod("verifyDocument")
            .parameterInfo

        assertThat(
            methodParameters.map { it.typeDescriptor.toString() },
            equalTo(expectedMethodParameters)
        )
    }

    @Test
    fun `Example usage - Verification Failure flow`(@TestParameter error: VerificationError) {
        lateinit var actual: VerificationError
        DocumentVerifier.exampleVerifierUsage(
            verifier = { _, _ ->
                throw VerificationResult.Failure(error)
            },
            document = mockk(relaxed = true),
            handleVerificationFailure = { actual = it },
            handleJourneyCompletion = {
                fail("Shouldn't have succeed the example flow!")
            }
        )

        assertThat(
            error,
            equalTo(actual)
        )
    }

    @Test
    fun `Example usage - Non-Verification Failure flow`() {
        val expected = Exception("This is a unit test")

        val exception = assertThrows(Exception::class.java) {
            DocumentVerifier.exampleVerifierUsage(
                verifier = { _, _ ->
                    throw expected
                },
                document = mockk(relaxed = true),
                handleVerificationFailure = {
                    fail("Shouldn't have been caught by verification failure handling!")
                },
                handleJourneyCompletion = {
                    fail("Shouldn't have succeed the example flow!")
                }
            )
        }

        assertEquals(expected, exception)
    }

    @Test
    fun `Example usage - Success flow`() {
        var hasSucceeded = false
        DocumentVerifier.exampleVerifierUsage(
            verifier = { _, _ ->
                VerificationResult.Success
            },
            document = mockk(relaxed = true),
            handleVerificationFailure = {
                fail("Received unexpected error: $it")
            },
            handleJourneyCompletion = {
                hasSucceeded = true
            }
        )

        assertTrue("Didn't successfully complete example usage!", hasSucceeded)
    }
}
