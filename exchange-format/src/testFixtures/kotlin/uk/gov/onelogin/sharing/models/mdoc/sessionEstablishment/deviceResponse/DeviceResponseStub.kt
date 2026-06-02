package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSignedStub.sharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSignedStub.sharingDeviceSignedJson
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedStub.sharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedStub.sharingIssuerSignedJson

object DeviceResponseStub {
    val document = SharingVerifiableDocumentWithPresentation(
        docType = "org.iso.18013.5.1.mDL",
        issuerSigned = sharingIssuerSigned,
        deviceSigned = sharingDeviceSigned
    )

    val documentJson =
        "{" +
            "\"docType\":\"${document.docType}\"," +
            "\"issuerSigned\":$sharingIssuerSignedJson," +
            "\"deviceSigned\":$sharingDeviceSignedJson" +
            "}"

    val successWithDocuments = DeviceResponse(
        documents = listOf(document)
    )
}
