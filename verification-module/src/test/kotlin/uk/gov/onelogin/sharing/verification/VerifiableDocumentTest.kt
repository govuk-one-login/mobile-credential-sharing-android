package uk.gov.onelogin.sharing.verification

import io.github.classgraph.ClassGraph
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class VerifiableDocumentTest {

    /**
     * DCMAW-20245: AC4: [VerifiableDocument] exposes docType and issuerSigned as defined.
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "getDocType" to String::class.java,
            "getIssuerSigned" to IssuerSigned::class.java
        )

        val classInfo = ClassGraph()
            .enableAllInfo()
            .acceptPackages(VerifiableDocument::class.java.packageName)
            .scan()
            .getClassInfo(VerifiableDocument::class.java.name)

        expectedMethods.forEach { (expectedName, expectedType) ->
            val methodInfo = classInfo.methodInfo.getSingleMethod(expectedName)
            assertThat(
                methodInfo.typeDescriptorStr,
                containsString(expectedType.name.replace(".", "/"))
            )
        }
    }
}