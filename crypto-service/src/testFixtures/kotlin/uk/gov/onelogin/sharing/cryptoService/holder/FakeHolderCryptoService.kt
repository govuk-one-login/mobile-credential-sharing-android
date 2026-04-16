package uk.gov.onelogin.sharing.cryptoService.holder

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

class FakeHolderCryptoService : HolderCryptoService {
    var encryptedToReturn: ByteArray = byteArrayOf()
    var encryptException: Exception? = null

    var lastBuildTerminationStatus: SessionDataStatus? = null
    var lastErrorDeviceResponseStatus: Status? = null
    var lastErrorSessionDataStatus: SessionDataStatus? = null

    var lastEncryptedDeviceResponse: DeviceResponse? = null
    var lastEncryptSkDevice: ByteArray? = null
    var lastEncryptCounter: UInt? = null

    override fun buildTerminationSessionData(status: SessionDataStatus): ByteArray {
        lastBuildTerminationStatus = status
        return byteArrayOf()
    }

    override fun buildErrorSessionData(
        deviceResponseStatus: Status,
        sessionDataStatus: SessionDataStatus,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        lastErrorDeviceResponseStatus = deviceResponseStatus
        lastErrorSessionDataStatus = sessionDataStatus
        return byteArrayOf()
    }

    override fun encryptDeviceResponse(
        deviceResponse: DeviceResponse,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray {
        encryptException?.let { throw it }
        lastEncryptedDeviceResponse = deviceResponse
        lastEncryptSkDevice = skDevice
        lastEncryptCounter = encryptCounter
        return encryptedToReturn
    }
}
