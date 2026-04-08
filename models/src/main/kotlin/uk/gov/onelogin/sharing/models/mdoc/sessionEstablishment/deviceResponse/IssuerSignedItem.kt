package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

data class IssuerSignedItem(
    val digestId: Long,
    val random: ByteArray,
    val elementIdentifier: String,
    val elementValue: Any
)
