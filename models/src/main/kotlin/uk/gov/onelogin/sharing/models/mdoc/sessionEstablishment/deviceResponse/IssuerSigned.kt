package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

data class IssuerSigned(
    val nameSpaces: Map<String, List<IssuerSignedItem>>?,
    val issuerAuth: ByteArray
)
