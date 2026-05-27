package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

fun interface DecryptDeviceResponseUseCase {
    /**
     * Decrypts [deviceResponseBytes] using AES-256-GCM with the [skDevice] session key
     * and decodes the plaintext CBOR into a [DeviceResponse] domain model.
     *
     * @return The decoded [DeviceResponse].
     * @throws DecryptDeviceResponseException if decryption or decoding fails.
     */
    operator fun invoke(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): DeviceResponse
}
