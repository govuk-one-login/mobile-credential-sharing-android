package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

/**
 * Handles cryptographic operations for the Verifier role.
 *
 * The Orchestrator delegates to this service during the verification lifecycle:
 * 1. [establishSession] — Decodes the QR code, generates ephemeral keys,
 *    calculates the Session Transcript, computes the shared secret, and derives
 *    the SKReader and SKDevice session keys.
 * 2. [encryptDeviceRequest] — Encrypts pre-built DeviceRequest CBOR bytes
 *    using the SKReader session key.
 */
interface VerifierCryptoService {
    fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    )

    /**
     * Encrypts [deviceRequestBytes] using AES-256-GCM with the [skReader] session key.
     *
     * @throws EncryptDeviceRequestException if encryption fails.
     */
    @Throws(EncryptDeviceRequestException::class)
    fun encryptDeviceRequest(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray

    /**
     * Constructs a [DeviceRequest] from the [itemsRequest] and encodes it to CBOR bytes.
     */
    fun buildDeviceRequest(itemsRequest: ItemsRequest): ByteArray

    /**
     * Constructs and CBOR-encodes a [SessionEstablishment] map from [eReaderKeyBytes]
     * and [encryptedDeviceRequest].
     *
     * @throws SessionEstablishmentException if construction or encoding fails.
     */
    @Throws(SessionEstablishmentException::class)
    fun buildSessionEstablishment(
        eReaderKeyBytes: ByteArray,
        encryptedDeviceRequest: ByteArray
    ): ByteArray

    fun deserializeSessionData(input: ByteArray): SessionData

    /**
     * Decrypts [deviceResponseBytes] using AES-256-GCM with the [skDevice] session key.
     *
     * @throws DecryptDeviceResponseException if decryption fails.
     */
    @Throws(DecryptDeviceResponseException::class)
    fun decryptDeviceResponse(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        decryptCounter: UInt
    ): ByteArray
}
