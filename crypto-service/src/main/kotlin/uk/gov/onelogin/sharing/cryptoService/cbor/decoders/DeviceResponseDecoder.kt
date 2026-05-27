package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

fun interface DeviceResponseDecoder {
    /**
     * Decodes a plaintext CBOR byte array into a [DeviceResponse] domain model.
     *
     * @param bytes The decrypted plaintext CBOR bytes.
     * @return [DeviceResponse] domain object.
     * @throws DeviceResponseDecodingException if the bytes cannot be decoded.
     */
    fun decode(bytes: ByteArray): DeviceResponse
}
