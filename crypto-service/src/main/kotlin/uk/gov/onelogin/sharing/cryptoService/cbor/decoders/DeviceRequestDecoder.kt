package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

fun interface DeviceRequestDecoder {
    /**
     * Decodes a device request object from a given bytearray
     *
     * @param bytes The encoded device request
     *
     * @return [DeviceRequest] object
     */
    fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest
}
