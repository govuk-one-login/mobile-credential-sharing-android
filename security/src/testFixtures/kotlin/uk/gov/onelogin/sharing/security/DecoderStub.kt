package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.security.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.security.cbor.dto.BleOptionsDto
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.security.cbor.dto.SecurityDto

object DecoderStub {
    const val VALID_ENCODED_DEVICE_ENGAGEMENT =
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib" +
            "6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="

    const val INVALID_CBOR =
        "gg8EaqoiWCA8Qib6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9A" +
            "pQERERESIiMzNERFVVVVVVVf____8="

    const val VALID_MDOC_URI = "mdoc:$VALID_ENCODED_DEVICE_ENGAGEMENT"

    const val VALID_TRANSCRIPT = "83d818587abf0063312e30019f01d818584cbf0102200121582093bc262949" +
        "91e6afa8cd9181d6e3c029f8bcba98a203c242f3c225820f046aaa2258203c4226fa6c27da6affb903c" +
        "42f7c211c7804f1d47f4747f2a3d999cd7b2c592dffff029f9f0201bf00f501f40a5011111111222233" +
        "334444555555555555ffffffffd818584ba401022001215820b3a42d21ea73d2f66760a8010b2cf3cfd" +
        "a7d0f598a1c8b93f4d6324099afc21f225820110ebb94d8fabf6c251777c37cea1dbde0a4e85ce7333b" +
        "5342944cfc6c4551e7f6"

    /**
     * [DeviceEngagementDto] representation of the [VALID_ENCODED_DEVICE_ENGAGEMENT] property.
     */
    val validDeviceEngagementDto = DeviceEngagementDto(
        version = "1.0",
        security = SecurityDto(
            cipherSuiteIdentifier = 1,
            ephemeralPublicKey = CoseKeyDto(
                keyType = 2L,
                curve = 1L,
                x = byteArrayOf(
                    -109,
                    -68,
                    38,
                    41,
                    73,
                    -111,
                    -26,
                    -81,
                    -88,
                    -51,
                    -111,
                    -127,
                    -42,
                    -29,
                    -64,
                    41,
                    -8,
                    -68,
                    -70,
                    -104,
                    -94,
                    3,
                    -62,
                    66,
                    -13,
                    -62,
                    37,
                    -126,
                    15,
                    4,
                    106,
                    -86
                ),
                y = byteArrayOf(
                    60,
                    66,
                    38,
                    -6,
                    108,
                    39,
                    -38,
                    106,
                    -1,
                    -71,
                    3,
                    -60,
                    47,
                    124,
                    33,
                    28,
                    120,
                    4,
                    -15,
                    -44,
                    127,
                    71,
                    71,
                    -14,
                    -93,
                    -39,
                    -103,
                    -51,
                    123,
                    44,
                    89,
                    45
                )
            )
        ),
        deviceRetrievalMethods = listOf(
            DeviceRetrievalMethodDto(
                type = 2,
                version = 1,
                options = BleOptionsDto(
                    serverMode = true,
                    clientMode = false,
                    peripheralServerModeUuid = byteArrayOf(
                        17, 17, 17, 17, 34, 34, 51, 51, 68, 68, 85, 85, 85, 85, 85, 85
                    )
                )
            )
        )
    )
}
