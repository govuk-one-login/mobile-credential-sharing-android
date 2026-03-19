package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.BleOptionsDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray

object DeviceRetrievalMethodsDeserializerStub {
    val expectedMethods = listOf(
        DeviceRetrievalMethodDto(
            type = 1,
            version = 1,
            options = BleOptionsDto(
                serverMode = true,
                clientMode = false,
                peripheralServerModeUuid = UUID_16_BIT.toByteArray()
            )
        )
    )

    val rawCborMock = listOf(
        listOf(1, 1, mapOf("0" to true, "1" to false, "10" to UUID_16_BIT.toByteArray()))
    )
}
