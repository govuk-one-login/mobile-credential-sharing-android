package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentTypeTest {

    @Test
    fun `Mdl value is ISO mDL document type`() {
        assertEquals("org.iso.18013.5.1.mDL", DocumentType.Mdl.value)
    }

    @Test
    fun `Custom returns provided value`() {
        assertEquals("org.example.custom.doc", DocumentType.Custom("org.example.custom.doc").value)
    }

    @Test
    fun `Custom with empty string`() {
        assertEquals("", DocumentType.Custom("").value)
    }

    @Test
    fun `Custom with special characters`() {
        assertEquals(
            "org.iso.18013.5.1.custom/type",
            DocumentType.Custom("org.iso.18013.5.1.custom/type").value
        )
    }
}
