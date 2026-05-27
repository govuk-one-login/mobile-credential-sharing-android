package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

/**
 * Data structure for the encoded
 * [uk.gov.onelogin.sharing.verification.format.document.IssuerSigned.nameSpaces] map value.
 */
data class IssuerSignedItem(
    val digestId: Long,
    val random: ByteArray,
    val elementIdentifier: String,
    val elementValue: Any
)
