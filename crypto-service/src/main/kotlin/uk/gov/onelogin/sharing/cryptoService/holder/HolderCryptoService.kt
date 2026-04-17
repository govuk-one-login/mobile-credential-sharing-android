package uk.gov.onelogin.sharing.cryptoService.holder

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

/**
 * Handles cryptographic operations for the Holder role.
 */
interface HolderCryptoService {
    /**
     * Constructs and CBOR-encodes a SessionData termination message.
     *
     * @param status The termination status code.
     * @return The CBOR-encoded SessionData bytes ready for transmission.
     */
    fun buildTerminationSessionData(status: SessionDataStatus): ByteArray

    /**
     * Builds a CBOR-encoded SessionData containing an encrypted error [DeviceResponse]
     * and a termination status code.
     *
     * @param deviceResponseStatus The status code for the error DeviceResponse.
     * @param sessionDataStatus The termination status code for the SessionData wrapper.
     * @param skDevice The session key for the device.
     * @param encryptCounter The current encryption message counter.
     * @return The CBOR-encoded SessionData bytes ready for transmission.
     */
    fun buildErrorSessionData(
        deviceResponseStatus: Status,
        sessionDataStatus: SessionDataStatus,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray

    /**
     * Encrypts a [DeviceResponse] for transmission to the Verifier.
     *
     * @param deviceResponse The response to encrypt.
     * @param skDevice The session key for the device.
     * @param encryptCounter The current encryption message counter.
     * @return The encrypted ciphertext + authentication tag bytes.
     */
    fun encryptDeviceResponse(
        deviceResponse: DeviceResponse,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray
}
