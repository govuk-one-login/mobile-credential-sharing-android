package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class SharingVerifiableDocumentWithPresentationTest {
    private val docType = "unit test"
    private val issuerSigned = SharingIssuerSigned(
        nameSpaces = null,
        issuerAuth = byteArrayOf(0, 1)
    )
    private val deviceSigned = SharingDeviceSigned(
        deviceNameSpacesBytes = byteArrayOf(1, 2),
        deviceSignature = byteArrayOf(2, 3)
    )
    private val document = SharingVerifiableDocumentWithPresentation(
        docType = docType,
        issuerSigned = issuerSigned,
        deviceSigned = deviceSigned
    )

    private val differentDocType = document.copy(
        docType = "another test"
    )
    private val differentIssuerAuth = document.copy(
        issuerSigned = issuerSigned.copy(
            issuerAuth = byteArrayOf(1, 2)
        )
    )
    private val differentDeviceSigned = document.copy(
        deviceSigned = deviceSigned.copy(
            deviceNameSpacesBytes = byteArrayOf(3, 4)
        )
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(document, document)
        assertEquals(document, document.copy())

        assertFalse(document.equals(null))
        assertFalse(document.equals("different type"))
        assertNotEquals(document, differentDocType)
        assertNotEquals(document, differentIssuerAuth)
        assertNotEquals(document, differentDeviceSigned)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(document.hashCode(), document.copy().hashCode())

        assertNotEquals(document.hashCode(), differentDocType.hashCode())
        assertNotEquals(document.hashCode(), differentIssuerAuth.hashCode())
        assertNotEquals(document.hashCode(), differentDeviceSigned.hashCode())
    }

    /**
     * DCMAW-20269: AC2: [SharingVerifiableDocumentWithPresentation.docType] returns the same value
     * as the underlying document's docType field.
     */
    @Test
    fun `Sharing implementation's document type are accessible via interface`() {
        val interfaceInstance = document as VerifiableDocument.WithPresentation

        assertEquals(
            document.docType,
            interfaceInstance.docType
        )
    }

    @Test
    fun `Sharing implementation's IssuerSigned is accessible via interface`() {
        val interfaceInstance = document as VerifiableDocument.WithPresentation

        assertEquals(
            document.issuerSigned,
            interfaceInstance.issuerSigned
        )
    }

    /**
     * DCMAW-20269: AC5: A Sharing SDK document with a device-signed portion can be wrapped in
     * [SharingVerifiableDocumentWithPresentation].
     */
    @Test
    fun `Sharing implementation's DeviceSigned is accessible via interface`() {
        val interfaceInstance = document as VerifiableDocument.WithPresentation

        assertEquals(
            document.deviceSigned,
            interfaceInstance.deviceSigned
        )
    }

    @Test
    fun `Verifiable documents can be passed in as a constructor property`() {
        val secondDocument = SharingVerifiableDocumentWithPresentation(
            document = document,
            deviceSigned = deviceSigned
        )

        assertEquals(
            document,
            secondDocument
        )
    }
}
