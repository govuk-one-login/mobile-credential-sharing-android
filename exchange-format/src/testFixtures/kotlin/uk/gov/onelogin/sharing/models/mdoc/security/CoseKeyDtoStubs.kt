package uk.gov.onelogin.sharing.models.mdoc.security

object CoseKeyDtoStubs {

    val validCoseKeyDtoBytes: ByteArray = (
        "a40102200121582060e3392385041f51403051f2415531cb56dd3f999c71687013aac6768bc8187" +
            "e225820e58deb8fdbe907f7dd5368245551a34796f7d2215c440c339bb0f7b67beccdfa"
        ).hexToByteArray()

    val validCoseKeyDto = CoseKeyDto(
        keyType = 2L,
        curve = 1L,
        x = byteArrayOf(
            96, -29, 57, 35, -123, 4, 31, 81, 64, 48, 81, -14, 65, 85, 49, -53, 86, -35, 63, -103,
            -100, 113, 104, 112, 19, -86, -58, 118, -117, -56, 24, 126
        ),
        y = byteArrayOf(
            -27, -115, -21, -113, -37, -23, 7, -9, -35, 83, 104, 36, 85, 81, -93, 71,
            -106, -9, -46, 33, 92, 68, 12, 51, -101, -80, -9, -74, 123, -20, -51, -6
        )
    )
}
