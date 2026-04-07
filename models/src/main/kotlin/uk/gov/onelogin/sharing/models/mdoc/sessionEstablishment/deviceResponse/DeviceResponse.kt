package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

data class DeviceResponse(
    val version: String = "1.0",
    val documents: List<Document>?,
    val documentErrors: Map<String, Status>?,
    val status: Status = Status.OK
)
