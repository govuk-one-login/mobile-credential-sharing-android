package uk.gov.onelogin.sharing.verification

import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceSignatures
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class IssuerSignedTest {
    /**
     * DCMAW-20245: AC4: [VerifiableDocument] exposes docType and issuerSigned as defined.
     */
    @Test
    fun `Ensure VerifiableDocument constraints`() {
        val expectedMethods = listOf(
            "getIssuerAuth" to ByteArray::class.java,
            "getNameSpaces" to Map::class.java
        )

        val classInfo = scanResult.getClassInfo(IssuerSigned::class.java.name)

        assertInterfaceSignatures(expectedMethods, classInfo)
    }
}