package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

object DeviceRequestStub {
    private const val MDL_DOC_TYPE = "org.iso.18013.5.1.mDL"
    private const val MDL_NAMESPACE = "org.iso.18013.5.1"

    val deviceRequestStub = deviceRequest(
        mapOf("age_over_18" to false)
    )

    fun deviceRequest(
        elements: Map<String, Boolean>,
        docType: String = MDL_DOC_TYPE,
        nameSpace: String = MDL_NAMESPACE
    ) = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                ItemsRequest(
                    docType = docType,
                    nameSpaces = mapOf(nameSpace to elements)
                )
            )
        )
    )
}
