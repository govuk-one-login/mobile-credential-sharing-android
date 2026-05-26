package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class SharingVerifiableDocumentTest {
    private val docType = "unit test"
    private val issuerSigned = SharingIssuerSigned(
        nameSpaces = null,
        issuerAuth = byteArrayOf(0, 1)
    )
    private val document = SharingVerifiableDocument(
        docType = docType,
        issuerSigned = issuerSigned
    )
    private val documentCopy = SharingVerifiableDocument(
        docType = docType,
        issuerSigned = issuerSigned.copy()
    )

    private val differentDocType = SharingVerifiableDocument(
        docType = "another test",
        issuerSigned = issuerSigned
    )
    private val differentIssuerSigned = SharingVerifiableDocument(
        docType = docType,
        issuerSigned = issuerSigned.copy(
            issuerAuth = byteArrayOf(1, 2)
        )
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(document, document)
        assertEquals(document, documentCopy)

        assertFalse(document.equals(null))
        assertFalse(document.equals("different type"))
        assertNotEquals(document, differentDocType)
        assertNotEquals(document, differentIssuerSigned)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(document.hashCode(), documentCopy.hashCode())

        assertNotEquals(document.hashCode(), differentDocType.hashCode())
        assertNotEquals(document.hashCode(), differentIssuerSigned.hashCode())
    }

    /**
     * DCMAW-20269: AC2: [SharingVerifiableDocument.docType] returns the same value as the
     * underlying document's docType field.
     */
    @Test
    fun `Sharing implementation's document type are accessible via interface`() {
        val interfaceInstance = document as VerifiableDocument

        assertEquals(
            document.docType,
            interfaceInstance.docType
        )
    }

    @Test
    fun `Sharing implementation's IssuerSigned is accessible via interface`() {
        val interfaceInstance = document as VerifiableDocument

        assertEquals(
            document.issuerSigned,
            interfaceInstance.issuerSigned
        )
    }

}