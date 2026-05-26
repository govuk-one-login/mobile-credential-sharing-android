package uk.gov.onelogin.sharing.models.deviceRequest

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class DeviceRequestModelTest {

    @Test
    fun `DeviceRequest model instantiates with example data`() {
        val mdlNameSpace = "org.iso.18013.5.1"
        val mdlDocType = "org.iso.18013.5.1.mDL"

        val requestedData = mapOf(
            "family_name" to true,
            "document_number" to true,
            "driving_privileges" to true,
            "issue_date" to true,
            "expiry_date" to true,
            "portrait" to false
        )

        val model = DeviceRequest(
            version = "1.0",
            docRequests = listOf(
                DocRequest(
                    itemsRequest = ItemsRequest(
                        docType = mdlDocType,
                        nameSpaces = mapOf(mdlNameSpace to requestedData)
                    )
                )
            )
        )

        assertEquals("1.0", model.version)
        assertEquals(1, model.docRequests.size)

        val modelItems = model.docRequests.first().itemsRequest
        assertEquals(mdlDocType, modelItems.docType)
        assertTrue(modelItems.nameSpaces.containsKey(mdlNameSpace))

        val modelNamespaces = modelItems.nameSpaces[mdlNameSpace]!!
        assertEquals(true, modelNamespaces["family_name"])
        assertEquals(true, modelNamespaces["document_number"])
        assertEquals(true, modelNamespaces["driving_privileges"])
        assertEquals(true, modelNamespaces["issue_date"])
        assertEquals(true, modelNamespaces["expiry_date"])
        assertEquals(false, modelNamespaces["portrait"])
        assertEquals(6, modelNamespaces.size)
    }
}
