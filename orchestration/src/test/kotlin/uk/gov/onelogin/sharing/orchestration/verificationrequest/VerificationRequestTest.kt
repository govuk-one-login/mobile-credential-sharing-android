package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationRequestTest {

    @Test
    fun `typed creates request with resolved document type and elements`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.GivenName to true,
                    MdlAttribute.FamilyName to true,
                    MdlAttribute.AgeOver(18) to false
                )
            )
        )

        assertEquals("org.iso.18013.5.1.mDL", request.documentType)
        assertEquals(
            listOf("given_name", "family_name", "age_over_18"),
            request.requestedElements
        )
    }

    @Test
    fun `typed with custom document type`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Custom("org.example.custom"),
            attributeGroup = AttributeGroup(mapOf(MdlAttribute.Portrait to false))
        )

        assertEquals("org.example.custom", request.documentType)
        assertEquals(listOf("portrait"), request.requestedElements)
    }

    @Test
    fun `typed with empty attribute group`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            attributeGroup = AttributeGroup(emptyMap())
        )

        assertEquals("org.iso.18013.5.1.mDL", request.documentType)
        assertTrue(request.requestedElements.isEmpty())
    }

    @Test
    fun `raw passes through strings directly`() {
        val request = VerificationRequest.raw(
            documentType = "some.doc.type",
            requestedElements = mapOf("field_a" to true, "field_b" to false)
        )

        assertEquals("some.doc.type", request.documentType)
        assertEquals(listOf("field_a", "field_b"), request.requestedElements)
    }

    @Test
    fun `raw with empty elements`() {
        val request = VerificationRequest.raw(
            documentType = "org.iso.18013.5.1.mDL",
            requestedElements = emptyMap()
        )

        assertTrue(request.requestedElements.isEmpty())
    }

    @Test
    fun `typed with invalid document type string via Custom`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Custom(""),
            attributeGroup = AttributeGroup(mapOf(MdlAttribute.GivenName to true))
        )

        assertEquals("", request.documentType)
    }

    @Test
    fun `data class equality`() {
        val a = VerificationRequest.raw("type", mapOf("a" to true))
        val b = VerificationRequest.raw("type", mapOf("a" to true))

        assertEquals(a, b)
    }

    @Test
    fun `attributeGroup preserves intentToRetain values`() {
        val request = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.GivenName to true,
                    MdlAttribute.FamilyName to false
                )
            )
        )

        assertEquals(true, request.attributeGroup.attributes[MdlAttribute.GivenName])
        assertEquals(false, request.attributeGroup.attributes[MdlAttribute.FamilyName])
    }
}
