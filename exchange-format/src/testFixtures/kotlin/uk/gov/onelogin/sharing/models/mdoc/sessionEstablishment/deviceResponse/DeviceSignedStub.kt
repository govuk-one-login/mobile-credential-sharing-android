package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

object DeviceSignedStub {
    val sharingDeviceSigned = SharingDeviceSigned(
        deviceNameSpacesBytes = byteArrayOf(0x03),
        deviceSignature = byteArrayOf(0x04)
    )
    val sharingDeviceSignedJson =
        "{" +
                "\"deviceNameSpacesBytes\":[3]," +
                "\"deviceSignature\":[4]" +
                "}"
}
