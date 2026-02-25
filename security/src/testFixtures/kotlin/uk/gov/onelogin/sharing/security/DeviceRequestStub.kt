package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

object DeviceRequestStub {
    fun deviceRequestStub() = DeviceRequest(
        version = "1.0",
        docRequests = listOf()
    )
}
