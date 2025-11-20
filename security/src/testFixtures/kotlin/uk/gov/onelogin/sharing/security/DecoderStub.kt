package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.security.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.cbor.dto.BleOptionsDto
import uk.gov.onelogin.sharing.security.cbor.dto.CoseKeyDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.security.cbor.dto.SecurityDto
import uk.gov.onelogin.sharing.security.cose.CoseKey

object DecoderStub {
    private val keyPair = generateValidKeyPair()
    private val coseKey = CoseKey.generateCoseKey(keyPair!!)

    const val VALID_CBOR =
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib" +
            "6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="
    const val INVALID_CBOR =
        "gg8EaqoiWCA8Qib6bCfaav-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap-fAgG_APUB9A" +
            "pQERERESIiMzNERFVVVVVVVf____8="

    val validDeviceEngagementDto = DeviceEngagementDto(
        version = "1.0",
        security = SecurityDto(
            cipherSuiteIdentifier = 1,
            ephemeralPublicKey = CoseKeyDto(
                keyType = 2,
                curve = 1,
                x = coseKey.x,
                y = coseKey.y
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
