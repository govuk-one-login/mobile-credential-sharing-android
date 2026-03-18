package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationRequestTest {

    @Test
    fun `typed creates request with resolved document type and elements`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            requestElements = listOf(
                RequestElement.GivenName,
                RequestElement.FamilyName,
                RequestElement.AgeOver(18)
            )
        )

        assertEquals("org.iso.18013.5.1.mDL", request.documentType)
        assertEquals(listOf("given_name", "family_name", "age_over_18"), request.requestedElements)
    }

    @Test
    fun `typed with custom document type`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Custom("org.example.custom"),
            requestElements = listOf(RequestElement.Portrait)
        )

        assertEquals("org.example.custom", request.documentType)
        assertEquals(listOf("portrait"), request.requestedElements)
    }

    @Test
    fun `typed with empty elements list`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            requestElements = emptyList()
        )

        assertEquals("org.iso.18013.5.1.mDL", request.documentType)
        assertTrue(request.requestedElements.isEmpty())
    }

    @Test
    fun `raw passes through strings directly`() {
        val request = VerificationRequest.raw(
            documentType = "some.doc.type",
            requestedElements = listOf("field_a", "field_b")
        )

        assertEquals("some.doc.type", request.documentType)
        assertEquals(listOf("field_a", "field_b"), request.requestedElements)
    }

    @Test
    fun `raw with empty strings`() {
        val request = VerificationRequest.raw(
            documentType = "",
            requestedElements = listOf("")
        )

        assertEquals("", request.documentType)
        assertEquals(listOf(""), request.requestedElements)
    }

    @Test
    fun `raw with empty elements list`() {
        val request = VerificationRequest.raw(
            documentType = "org.iso.18013.5.1.mDL",
            requestedElements = emptyList()
        )

        assertTrue(request.requestedElements.isEmpty())
    }

    @Test
    fun `typed with invalid document type string via Custom`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Custom(""),
            requestElements = listOf(RequestElement.GivenName)
        )

        assertEquals("", request.documentType)
    }

    @Test
    fun `data class equality`() {
        val a = VerificationRequest.raw("type", listOf("a"))
        val b = VerificationRequest.raw("type", listOf("a"))

        assertEquals(a, b)
    }

    @Test
    fun `typed and raw produce equal result for same values`() {
        val typed = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            requestElements = listOf(RequestElement.GivenName)
        )
        val raw = VerificationRequest.raw(
            documentType = "org.iso.18013.5.1.mDL",
            requestedElements = listOf("given_name")
        )

        assertEquals(typed, raw)
    }
}
