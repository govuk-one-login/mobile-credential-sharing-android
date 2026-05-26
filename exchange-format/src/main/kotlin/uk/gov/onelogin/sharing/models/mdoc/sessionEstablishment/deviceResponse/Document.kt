package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

data class Document(
    val docType: String,
    val issuerSigned: IssuerSigned,
    val deviceSigned: DeviceSigned
)
