package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

fun interface EncryptDeviceResponseUseCase {
    fun execute(
        deviceResponse: DeviceResponse,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray
}
