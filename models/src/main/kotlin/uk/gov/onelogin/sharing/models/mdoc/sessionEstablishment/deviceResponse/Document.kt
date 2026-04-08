package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

data class Document(
    val docType: String,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned
)
