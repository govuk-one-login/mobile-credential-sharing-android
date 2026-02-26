package uk.gov.onelogin.sharing.security.cbor.decoders

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

class FakeDeviceRequestDecoder(val deviceRequestToReturn: DeviceRequest) : DeviceRequestDecoder {
    var lastPlaintext: ByteArray? = null

    override fun deviceRequestDecoder(bytes: ByteArray): DeviceRequest {
        lastPlaintext = bytes
        return deviceRequestToReturn
    }
}
