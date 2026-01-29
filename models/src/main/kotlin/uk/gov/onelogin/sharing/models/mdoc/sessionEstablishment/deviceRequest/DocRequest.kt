package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

data class DocRequest (
    val docType: String,
    val nameSpaces: Map<String, Map<String, Boolean>>,
    val itemRequestBytes: ByteArray
)
