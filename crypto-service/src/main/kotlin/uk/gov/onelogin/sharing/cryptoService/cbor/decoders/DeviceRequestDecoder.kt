package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

fun interface DeviceRequestDecoder {
    /**
     * Decodes a device request object from a given bytearray
     *
     * @param bytes The encoded device request
     *
     * @return [DeviceRequest] object
     * @throws DeviceRequestDecodingException if the bytes cannot be decoded as a valid DeviceRequest
     */
    fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest
}
