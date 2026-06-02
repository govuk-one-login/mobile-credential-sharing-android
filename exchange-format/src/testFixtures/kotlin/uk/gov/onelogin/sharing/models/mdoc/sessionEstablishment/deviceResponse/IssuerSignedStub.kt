package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

object IssuerSignedStub {
    val sharingNameSpace = "org.iso.18013.5.1"
    private val sharingNameSpaceBytes = byteArrayOf(0x01)
    val sharingIssuerSigned = SharingIssuerSigned(
        nameSpaces = mapOf(sharingNameSpace to listOf(sharingNameSpaceBytes)),
        issuerAuth = byteArrayOf(0x02)
    )
    val sharingIssuerSignedJson =
        "{" +
                "\"nameSpaces\":{" +
                "\"$sharingNameSpace\":[[1]]" +
                "}," +
                "\"issuerAuth\":[2]" +
                "}"
}
