package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.toItemsRequest

class VerifierAttributeOptionTest {
    private val option = VerifierAttributeOption.PORTRAIT_AND_DRIVING_PRIVILEGES_RETAIN

    @Test
    fun `Portrait and Driving Privileges option has the expected display name`() {
        assertEquals(
            "Portrait and Driving Privileges (Retain)",
            option.displayName
        )
    }

    @Test
    fun `Portrait and Driving Privileges option requests both attributes with intent to retain`() {
        val itemsRequest = option.attributeGroup.toItemsRequest(DocumentType.Mdl)

        assertEquals(DocumentType.Mdl.value, itemsRequest.docType)
        assertEquals(
            mapOf(
                DocumentType.Mdl.NAMESPACE to mapOf(
                    "portrait" to true,
                    "driving_privileges" to true
                )
            ),
            itemsRequest.nameSpaces
        )
    }

    @Test
    fun `Portrait and Driving Privileges option uses a single document request`() {
        assertEquals(DocRequestMode.SINGLE, option.docRequestMode)
    }
}
