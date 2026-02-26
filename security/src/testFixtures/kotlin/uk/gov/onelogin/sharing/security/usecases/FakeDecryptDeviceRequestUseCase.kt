package uk.gov.onelogin.sharing.security.usecases

import java.security.PrivateKey
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.security.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.security.cryptography.usecases.DecryptDeviceRequestUseCase

class FakeDecryptDeviceRequestUseCase : DecryptDeviceRequestUseCase {
    override fun execute(
        sessionEstablishmentBytes: ByteArray,
        engagement: String,
        holderPrivateKey: PrivateKey
    ): DeviceRequest = deviceRequestStub()
}
