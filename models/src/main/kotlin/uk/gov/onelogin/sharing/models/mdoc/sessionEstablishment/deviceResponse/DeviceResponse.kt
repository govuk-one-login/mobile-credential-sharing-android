package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

data class DeviceResponse(
    val version: String,
    val documents: List<Document>?,
    val documentErrors: Map<String, Long>?,
    val status: Long
)
