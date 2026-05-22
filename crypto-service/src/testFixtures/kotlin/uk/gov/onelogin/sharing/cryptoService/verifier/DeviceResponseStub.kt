package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned

object DeviceResponseStub {
    val document = Document(
        docType = "org.iso.18013.5.1.mDL",
        issuerSigned = IssuerSigned(
            nameSpaces = mapOf("org.iso.18013.5.1" to listOf(byteArrayOf(0x01))),
            issuerAuth = byteArrayOf(0x02)
        ),
        deviceSigned = DeviceSigned(
            nameSpaces = byteArrayOf(0x03),
            deviceAuth = byteArrayOf(0x04)
        )
    )

    val successWithDocuments = DeviceResponse(
        documents = listOf(document)
    )
}
