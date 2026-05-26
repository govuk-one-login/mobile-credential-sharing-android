package uk.gov.onelogin.sharing.verification.format.document

import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.ClassInfoExt.assertInterfaceReturnTypes
import uk.gov.onelogin.sharing.verification.format.ClassInfoExt.scanResult

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
