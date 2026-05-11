package uk.gov.onelogin.sharing.orchestration.verifier.credential

import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException

fun interface DeviceRequestHandler {
    /**
     * Builds and encrypts the DeviceRequest using the [skReader] session key and [encryptCounter].
     *
     * @throws EncryptDeviceRequestException if encryption fails.
     */
    @Throws(EncryptDeviceRequestException::class)
    fun buildAndEncrypt(skReader: ByteArray, encryptCounter: UInt): ByteArray
}
