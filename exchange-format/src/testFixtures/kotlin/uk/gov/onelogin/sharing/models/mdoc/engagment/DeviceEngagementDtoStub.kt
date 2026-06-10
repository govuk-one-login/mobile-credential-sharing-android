package uk.gov.onelogin.sharing.models.mdoc.engagment

import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptionsDto
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptionsDtoStub.UUID_16_BIT
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto

object DeviceEngagementDtoStub {
    const val VALID_ENCODED_DEVICE_ENGAGEMENT =
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib" +
            "6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="

    val validDeviceEngagementDto = DeviceEngagementDto(
        version = "1.0",
        security = SecurityDto(
            cipherSuiteIdentifier = 1,
            eDeviceKeyBytes = (
                "bf0102200121582093bc26294991e6afa8cd9181d6e3c029f8bcba98a2" +
                    "03c242f3c225820f046aaa2258203c4226fa6c27da6affb903c42f7c211c7804f1d47f" +
                    "4747f2a3d999cd7b2c592dff"
                ).hexToByteArray(),
            ephemeralPublicKey = CoseKeyDto(
                keyType = 2L,
                curve = 1L,
                x = byteArrayOf(
                    -109, -68, 38, 41, 73, -111, -26, -81, -88, -51, -111, -127, -42, -29, -64,
                    41, -8, -68, -70, -104, -94, 3, -62, 66, -13, -62, 37, -126, 15, 4, 106, -86
                ),
                y = byteArrayOf(
                    60, 66, 38, -6, 108, 39, -38, 106, -1, -71, 3, -60, 47, 124, 33, 28, 120, 4,
                    -15, -44, 127, 71, 71, -14, -93, -39, -103, -51, 123, 44, 89, 45
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
                    peripheralServerModeUuid = UUID_16_BIT.toByteArray()
                )
            )
        )
    )
}
