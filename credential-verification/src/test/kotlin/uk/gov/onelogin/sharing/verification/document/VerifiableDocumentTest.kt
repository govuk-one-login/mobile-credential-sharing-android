package uk.gov.onelogin.sharing.verification.document

import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.document.models.DeviceSigned
import uk.gov.onelogin.sharing.verification.document.models.IssuerSigned

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

        val classInfo = scanResult.getClassInfo(VerifiableDocument::class.java.name)

        assertInterfaceReturnTypes(expectedMethods, classInfo)
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
            "getDeviceSigned" to DeviceSigned::class.java
        )

        val classInfo = scanResult.getClassInfo(
            VerifiableDocument.WithPresentation::class.java.name
        )

        assertInterfaceReturnTypes(expectedMethods, classInfo)
    }
}
