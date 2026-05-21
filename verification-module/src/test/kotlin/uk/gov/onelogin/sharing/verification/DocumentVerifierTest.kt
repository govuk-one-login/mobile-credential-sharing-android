package uk.gov.onelogin.sharing.verification

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.result.VerificationResult

class DocumentVerifierTest {
    /**
     * DCMAW-20245: AC8: [DocumentVerifier] exposes `verifyDocument(document, sessionTranscript?)`
     * returning [uk.gov.onelogin.sharing.verification.result.VerificationResult.Success].
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "verifyDocument" to VerificationResult.Success::class.java,
        )
        val expectedMethodParameters = listOf(
            VerifiableDocument::class,
            SessionTranscript::class,
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
}