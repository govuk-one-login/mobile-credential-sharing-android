package uk.gov.onelogin.sharing.cryptoService.usecases

import uk.gov.onelogin.sharing.cryptoService.verifier.DecryptDeviceResponseUseCase

class FakeDecryptDeviceResponseUseCase : DecryptDeviceResponseUseCase {
    var plaintextToReturn: ByteArray = byteArrayOf(0x01, 0x02, 0x03)
    var exception: Exception? = null
    var lastDeviceResponseBytes: ByteArray? = null
    var lastSkDevice: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun invoke(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        lastDeviceResponseBytes = deviceResponseBytes
        lastSkDevice = skDevice
        lastEncryptCounter = encryptCounter
        exception?.let { throw it }
        return plaintextToReturn
    }
}
