package uk.gov.onelogin.sharing.verification.document.models

import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class IssuerSignedTest {
    /**
     * DCMAW-20245: AC5: [IssuerSigned] exposes `issuerAuth: ByteArray` and
     * `nameSpaces: Map<String, ByteArray>` as defined.
     */
    @Test
    fun `Ensure IssuerSigned constraints`() {
        val expectedMethods = listOf(
            "getIssuerAuth" to ByteArray::class.java,
            "getNameSpaces" to Map::class.java
        )

        val classInfo = scanResult.getClassInfo(IssuerSigned::class.java.name)

        assertInterfaceReturnTypes(expectedMethods, classInfo)
    }
}
