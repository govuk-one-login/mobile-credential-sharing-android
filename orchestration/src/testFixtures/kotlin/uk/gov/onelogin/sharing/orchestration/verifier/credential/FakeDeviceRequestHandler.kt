package uk.gov.onelogin.sharing.orchestration.verifier.credential

import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException

class FakeDeviceRequestHandler(
    private val encryptedToReturn: ByteArray = byteArrayOf(0x01, 0x02),
    private val exceptionToThrow: EncryptDeviceRequestException? = null
) : DeviceRequestHandler {

    var lastSkReader: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun buildAndEncrypt(skReader: ByteArray, encryptCounter: UInt): ByteArray {
        lastSkReader = skReader
        lastEncryptCounter = encryptCounter
        exceptionToThrow?.let { throw it }
        return encryptedToReturn
    }
}
