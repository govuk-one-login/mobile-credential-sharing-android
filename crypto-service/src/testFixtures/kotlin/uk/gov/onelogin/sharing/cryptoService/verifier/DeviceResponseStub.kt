package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocumentWithPresentation

object DeviceResponseStub {
    val document = SharingVerifiableDocumentWithPresentation(
        docType = "org.iso.18013.5.1.mDL",
        issuerSigned = SharingIssuerSigned(
            nameSpaces = mapOf("org.iso.18013.5.1" to listOf(byteArrayOf(0x01))),
            issuerAuth = byteArrayOf(0x02)
        ),
        deviceSigned = SharingDeviceSigned(
            deviceNameSpacesBytes = byteArrayOf(0x03),
            deviceSignature = byteArrayOf(0x04)
        )
    )

    val successWithDocuments = DeviceResponse(
        documents = listOf(document)
    )
}
