package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE

class AttributeGroupMapperTest {

    @Test
    fun `toItemsRequest sets docType to MDL document type`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(21) to false
            )
        )

        val result = attributeGroup.toItemsRequest(DocumentType.Mdl)

        assertEquals(MDL_DOC_TYPE, result.docType)
    }

    @Test
    fun `toItemsRequest contains MDL namespace key`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(21) to false
            )
        )

        val result = attributeGroup.toItemsRequest(DocumentType.Mdl)

        assertTrue(result.nameSpaces.containsKey(MDL_NAMESPACE))
    }

    @Test
    fun `toItemsRequest maps portrait and age_over_21 with intentToRetain false`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(21) to false
            )
        )

        val elements = attributeGroup.toItemsRequest(DocumentType.Mdl)
            .nameSpaces[MDL_NAMESPACE]!!

        assertEquals(mapOf("portrait" to false, "age_over_21" to false), elements)
    }

    @Test
    fun `toItemsRequest does not include attributes outside the input group age_over_21`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(21) to false
            )
        )

        val elements = attributeGroup.toItemsRequest(DocumentType.Mdl)
            .nameSpaces[MDL_NAMESPACE]!!

        assertFalse(elements.containsKey("given_name"))
        assertFalse(elements.containsKey("family_name"))
        assertFalse(elements.containsKey("age_over_18"))
        assertEquals(2, elements.size)
    }

    @Test
    fun `maps given_name and family_name with intentToRetain true and age_over_18 false`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.GivenName to true,
                MdlAttribute.FamilyName to true,
                MdlAttribute.AgeOver(18) to false
            )
        )

        val elements = attributeGroup.toItemsRequest(DocumentType.Mdl)
            .nameSpaces[MDL_NAMESPACE]!!

        assertEquals(true, elements["given_name"])
        assertEquals(true, elements["family_name"])
        assertEquals(false, elements["age_over_18"])
    }

    @Test
    fun `toItemsRequest does not include attributes outside the input group age_over_18`() {
        val attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.GivenName to true,
                MdlAttribute.FamilyName to true,
                MdlAttribute.AgeOver(18) to false
            )
        )

        val elements = attributeGroup.toItemsRequest(DocumentType.Mdl)
            .nameSpaces[MDL_NAMESPACE]!!

        assertFalse(elements.containsKey("portrait"))
        assertFalse(elements.containsKey("age_over_21"))
        assertEquals(3, elements.size)
    }

    @Test
    fun `toItemsRequest with Custom doc type uses custom value as both docType and namespace`() {
        val attributeGroup = AttributeGroup(mapOf(MdlAttribute.GivenName to false))

        val result = attributeGroup.toItemsRequest(DocumentType.Custom("org.example.doc"))

        assertEquals("org.example.doc", result.docType)
        assertTrue(result.nameSpaces.containsKey("org.example.doc"))
    }

    @Test
    fun `toItemsRequest String overload resolves MDL docType to Mdl DocumentType`() {
        val attributeGroup = AttributeGroup(mapOf(MdlAttribute.Portrait to false))

        val result = attributeGroup.toItemsRequest(MDL_DOC_TYPE)

        assertEquals(MDL_DOC_TYPE, result.docType)
        assertTrue(result.nameSpaces.containsKey(MDL_NAMESPACE))
    }

    @Test
    fun `toItemsRequest String overload treats unknown docType as Custom`() {
        val attributeGroup = AttributeGroup(mapOf(MdlAttribute.GivenName to false))

        val result = attributeGroup.toItemsRequest("org.example.doc")

        assertEquals("org.example.doc", result.docType)
        assertTrue(result.nameSpaces.containsKey("org.example.doc"))
    }

    @Test
    fun `toItemsRequest maps both standard and GB attributes to their correct namespaces`() {
        val attributeGroup = AttributeGroup(
            attributes = mapOf(
                MdlAttribute.GivenName to true,
                MdlAttribute.AgeOver(21) to false
            ),
            gbAttributes = mapOf(
                GbAttribute.Title to true
            )
        )

        val result = attributeGroup.toItemsRequest(DocumentType.Mdl)

        assertEquals(2, result.nameSpaces.size)
        assertTrue(result.nameSpaces.containsKey(MDL_NAMESPACE))
        assertTrue(result.nameSpaces.containsKey(GbAttribute.NAMESPACE))

        val standardElements = result.nameSpaces[MDL_NAMESPACE]!!
        assertEquals(true, standardElements["given_name"])
        assertEquals(false, standardElements["age_over_21"])
        assertFalse(standardElements.containsKey("title"))

        val gbElements = result.nameSpaces[GbAttribute.NAMESPACE]!!
        assertEquals(true, gbElements["title"])
        assertFalse(gbElements.containsKey("given_name"))
        assertFalse(gbElements.containsKey("age_over_23"))
    }

    @Test
    fun `toItemsRequest with only GB attributes`() {
        val attributeGroup = AttributeGroup(
            gbAttributes = mapOf(
                GbAttribute.WelshLicence to false
            ),
            attributes = emptyMap()
        )

        val result = attributeGroup.toItemsRequest(DocumentType.Mdl)

        assertEquals(1, result.nameSpaces.size)
        assertTrue(result.nameSpaces.containsKey(GbAttribute.NAMESPACE))
        assertEquals(mapOf("welsh_licence" to false), result.nameSpaces[GbAttribute.NAMESPACE])
    }
}
