package uk.gov.onelogin.sharing.cryptoService.verifier

fun interface DecryptDeviceResponseUseCase {
    /**
     * Decrypts [deviceResponseBytes] using AES-256-GCM with the [skDevice] session key.
     *
     * @return The decrypted bytes
     * @throws DecryptDeviceResponseException if encryption fails.
     */
    operator fun invoke(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray
}