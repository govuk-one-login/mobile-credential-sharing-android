package uk.gov.onelogin.sharing.cryptoService.verifier

fun interface EncryptDeviceRequestUseCase {
    /**
     * Encrypts [deviceRequestBytes] using AES-256-GCM with the [skReader] session key.
     *
     * The IV is constructed as [Verifier Identifier (8 zero bytes)] || [encryptCounter (4 bytes)].
     *
     * @return The encrypted bytes as Ciphertext || Authentication Tag (16 bytes).
     * @throws EncryptDeviceRequestException if encryption fails.
     */
    fun encrypt(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray
}
