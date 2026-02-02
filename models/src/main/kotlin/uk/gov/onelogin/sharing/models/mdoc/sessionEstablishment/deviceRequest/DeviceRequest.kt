package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

data class DeviceRequest(val version: String = "1.0", val docRequests: List<DocRequest>)
