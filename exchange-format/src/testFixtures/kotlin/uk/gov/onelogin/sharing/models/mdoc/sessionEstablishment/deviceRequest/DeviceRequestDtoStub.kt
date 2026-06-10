package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

object DeviceRequestDtoStub {
    const val MDL_DOC_TYPE = "org.iso.18013.5.1.mDL"
    const val MDL_NAMESPACE = "org.iso.18013.5.1"
    const val CBOR_TAG_24_BYTE_0 = 0xD8
    const val CBOR_TAG_24_BYTE_1 = 0x18

    val deviceRequestStub = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                ItemsRequest(
                    docType = MDL_DOC_TYPE,
                    nameSpaces = mapOf(MDL_NAMESPACE to mapOf("age_over_18" to false))
                )
            )
        )
    )
}
