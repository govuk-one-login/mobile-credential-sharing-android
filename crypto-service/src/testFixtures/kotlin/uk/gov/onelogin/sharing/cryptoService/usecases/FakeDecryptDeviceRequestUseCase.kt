package uk.gov.onelogin.sharing.cryptoService.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.DecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

class FakeDecryptDeviceRequestUseCase : DecryptDeviceRequestUseCase {
    override fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey,
        decryptCounter: UInt
    ): DeviceRequest = deviceRequestStub
}
