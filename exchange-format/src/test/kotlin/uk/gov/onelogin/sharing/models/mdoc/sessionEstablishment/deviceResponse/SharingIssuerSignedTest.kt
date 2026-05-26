package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

class SharingIssuerSignedTest {

    private val issuerSigned = SharingIssuerSigned(
        issuerAuth = byteArrayOf(0, 1),
        nameSpaces = mapOf("unit test" to listOf(byteArrayOf(1, 2)))
    )

    private val differentAuth = issuerSigned.copy(
        issuerAuth = byteArrayOf(1, 2)
    )

    private val differentNameSpaces = issuerSigned.copy(
        nameSpaces = mapOf("another test" to listOf(byteArrayOf(2, 3)))
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(issuerSigned, issuerSigned)
        assertEquals(issuerSigned, issuerSigned.copy())

        assertFalse(issuerSigned.equals(null))
        assertFalse(issuerSigned.equals("different type"))
        assertNotEquals(issuerSigned, differentNameSpaces)
        assertNotEquals(issuerSigned, differentAuth)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(issuerSigned.hashCode(), issuerSigned.copy().hashCode())

        assertNotEquals(issuerSigned.hashCode(), differentNameSpaces.hashCode())
        assertNotEquals(issuerSigned.hashCode(), differentAuth.hashCode())
    }

    /**
     * DCMAW-20269: AC3: [SharingIssuerSigned.issuerAuth] returns the same bytes as the
     * underlying document's issuer auth field.
     */
    @Test
    fun `Sharing implementation's IssuerAuth are accessible via interface`() {
        val interfaceInstance = issuerSigned as IssuerSigned

        assertEquals(
            issuerSigned.issuerAuth,
            interfaceInstance.issuerAuth
        )
    }

    /**
     * DCMAW-20269: AC4: [SharingIssuerSigned.nameSpaces] returns the same namespace keys as the
     * underlying document; each list entry is the original Tag-24 byte sequence, unchanged.
     */
    @Test
    fun `Sharing implementation's name spaces are accessible via interface`() {
        val interfaceInstance = issuerSigned as IssuerSigned

        assertEquals(
            issuerSigned.nameSpaces,
            interfaceInstance.nameSpaces
        )
    }
}
