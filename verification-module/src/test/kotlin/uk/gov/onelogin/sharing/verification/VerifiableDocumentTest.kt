package uk.gov.onelogin.sharing.verification

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class VerifiableDocumentTest {
    private val scanResult = ClassGraph()
        .enableAllInfo()
        .acceptPackages(VerifiableDocument::class.java.packageName)
        .scan()

    /**
     * DCMAW-20245: AC4: [VerifiableDocument] exposes docType and issuerSigned as defined.
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "getDocType" to String::class.java,
            "getIssuerSigned" to IssuerSigned::class.java
        )

        val classInfo = scanResult.getClassInfo(VerifiableDocument::class.java.name)

        assertInterfaceSignatures(expectedMethods, classInfo)
    }

    /**
     * DCMAW-20245: AC6: VerifiableDocumentWithPresentation extends VerifiableDocument and
     * exposes deviceSigned: DeviceSigned
     */
    @Test
    fun `Ensure VerifiableDocument$WithPresentation constraints`() {
        val expectedMethods = listOf(
            "getDocType" to String::class.java,
            "getIssuerSigned" to IssuerSigned::class.java,
            "getDeviceSigned" to DeviceSigned::class.java,
        )

        val classInfo = scanResult.getClassInfo(
            VerifiableDocument.WithPresentation::class.java.name
        )

        assertInterfaceSignatures(expectedMethods, classInfo)
    }

    private fun assertInterfaceSignatures(
        expectedMethods: List<Pair<String, Class<out Any>>>,
        classInfo: ClassInfo,
    ) {
        expectedMethods.forEach { (expectedName, expectedType) ->
            val methodInfo = classInfo.methodInfo.getSingleMethod(expectedName)
            assertThat(
                methodInfo.typeDescriptorStr,
                containsString(expectedType.name.replace(".", "/"))
            )
        }
    }
}