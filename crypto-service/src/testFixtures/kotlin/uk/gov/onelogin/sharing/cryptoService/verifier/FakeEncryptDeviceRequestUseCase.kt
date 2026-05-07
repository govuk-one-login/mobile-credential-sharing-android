package uk.gov.onelogin.sharing.cryptoService.verifier

class FakeEncryptDeviceRequestUseCase(
    private val encryptedToReturn: ByteArray = byteArrayOf(0x01, 0x02),
    private val exceptionToThrow: EncryptDeviceRequestException? = null
) : EncryptDeviceRequestUseCase {

    var lastDeviceRequestBytes: ByteArray? = null
    var lastSkReader: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun encrypt(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        lastDeviceRequestBytes = deviceRequestBytes
        lastSkReader = skReader
        lastEncryptCounter = encryptCounter
        exceptionToThrow?.let { throw it }
        return encryptedToReturn
    }
}
