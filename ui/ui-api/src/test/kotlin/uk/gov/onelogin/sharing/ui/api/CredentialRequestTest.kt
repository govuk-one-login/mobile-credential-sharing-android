package uk.gov.onelogin.sharing.ui.api

import org.junit.Assert.assertEquals
import org.junit.Test

class CredentialRequestTest {
    @Test
    fun `creates CredentialRequest with document types`() {
        val request = CredentialRequest(documentTypes = listOf("org.iso.18013.5.1.mDL"))

        assertEquals(1, request.documentTypes.size)
        assertEquals("org.iso.18013.5.1.mDL", request.documentTypes[0])
    }
}
