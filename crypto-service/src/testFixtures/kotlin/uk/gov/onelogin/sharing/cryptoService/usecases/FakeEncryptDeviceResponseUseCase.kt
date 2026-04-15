package uk.gov.onelogin.sharing.cryptoService.usecases

import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.EncryptDeviceResponseUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

class FakeEncryptDeviceResponseUseCase : EncryptDeviceResponseUseCase {
    var encryptedToReturn: ByteArray = byteArrayOf(0x0A, 0x0B)

    override fun execute(
        deviceResponse: DeviceResponse,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray = encryptedToReturn
}
