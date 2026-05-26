package uk.gov.onelogin.sharing.verification.format.document

import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.ClassInfoExt
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

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

        val classInfo = ClassInfoExt.scanResult.getClassInfo(VerifiableDocument::class.java.name)

        ClassInfoExt.assertInterfaceReturnTypes(expectedMethods, classInfo)
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

        val classInfo = ClassInfoExt.scanResult.getClassInfo(
            VerifiableDocument.WithPresentation::class.java.name
        )

        ClassInfoExt.assertInterfaceReturnTypes(expectedMethods, classInfo)
    }
}