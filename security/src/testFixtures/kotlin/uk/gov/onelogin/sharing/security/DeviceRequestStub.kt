package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

object DeviceRequestStub {
    val deviceRequestStub = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                ItemsRequest(
                    docType = "MDL",
                    nameSpaces = mapOf(
                        "rg.iso.18013.5.1" to mapOf(
                            "age_over_18" to false
                        )
                    )
                )
            )
        )
    )
}
