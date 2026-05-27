package uk.gov.onelogin.sharing.cryptoService.usecases

import uk.gov.onelogin.sharing.cryptoService.verifier.DecryptDeviceResponseUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

class FakeDecryptDeviceResponseUseCase : DecryptDeviceResponseUseCase {
    var fakeDeviceResponse: DeviceResponse = DeviceResponse()
    var exception: Exception? = null
    var lastDeviceResponseBytes: ByteArray? = null
    var lastSkDevice: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun invoke(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): DeviceResponse {
        lastDeviceResponseBytes = deviceResponseBytes
        lastSkDevice = skDevice
        lastEncryptCounter = encryptCounter
        exception?.let { throw it }
        return fakeDeviceResponse
    }
}
