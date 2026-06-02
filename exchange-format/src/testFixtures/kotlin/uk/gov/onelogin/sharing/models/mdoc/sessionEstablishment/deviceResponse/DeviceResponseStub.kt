package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

object DeviceResponseStub {
    private val sharingNameSpace = "org.iso.18013.5.1"
    private val sharingNameSpaceBytes = byteArrayOf(0x01)
    private val sharingIssuerSigned = SharingIssuerSigned(
        nameSpaces = mapOf(sharingNameSpace to listOf(sharingNameSpaceBytes)),
        issuerAuth = byteArrayOf(0x02)
    )
    private val sharingDeviceSigned = SharingDeviceSigned(
        deviceNameSpacesBytes = byteArrayOf(0x03),
        deviceSignature = byteArrayOf(0x04)
    )
    val document = SharingVerifiableDocumentWithPresentation(
        docType = "org.iso.18013.5.1.mDL",
        issuerSigned = sharingIssuerSigned,
        deviceSigned = sharingDeviceSigned
    )

    val documentJson =
        "{" +
                "\"docType\":\"${document.docType}\"," +
                "\"issuerSigned\":{" +
                "\"nameSpaces\":{" +
                "\"$sharingNameSpace\":[[1]]" +
                "}," +
                "\"issuerAuth\":[2]" +
                "}," +
                "\"deviceSigned\":{" +
                "\"deviceNameSpacesBytes\":[3]," +
                "\"deviceSignature\":[4]" +
                "}" +
                "}"

    val successWithDocuments = DeviceResponse(
        documents = listOf(document)
    )
}
