package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.BleOptionsDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SecurityDto

object DecoderStub {
    const val VALID_ENCODED_DEVICE_ENGAGEMENT =
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib" +
            "6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="

    const val INVALID_CBOR =
        "gg8EaqoiWCA8Qib6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9A" +
            "pQERERESIiMzNERFVVVVVVVf____8="

    const val VALID_MDOC_URI = "mdoc:$VALID_ENCODED_DEVICE_ENGAGEMENT"

    const val INVALID_VALID_MDOC_URI = "https:$VALID_ENCODED_DEVICE_ENGAGEMENT"

    const val VALID_TRANSCRIPT =
        "83d818587abf0063312e30019f01d818584cbf0102200121582093bc26294991e6afa8cd9181d6e3c029f8" +
            "bcba98a203c242f3c225820f046aaa2258203c4226fa6c27da6affb903c42f7c211c7804f1d47f" +
            "4747f2a3d999cd7b2c592dffff029f9f0201bf00f501f40a501111111122223333444455555555" +
            "5555ffffffffd818584ba40102200121582060e3392385041f51403051f2415531cb56dd3f999c" +
            "71687013aac6768bc8187e225820e58deb8fdbe907f7dd5368245551a34796f7d2215c440c339b" +
            "b0f7b67beccdfaf6"

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
