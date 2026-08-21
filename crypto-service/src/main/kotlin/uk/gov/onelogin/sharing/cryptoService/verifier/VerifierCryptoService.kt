package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse

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
     * Builds the detached ReaderAuthenticationBytes payload.
     *
     * @param sessionTranscript The raw (untagged) SessionTranscript CBOR array.
     * @param itemsRequestBytes The CBOR Tag 24 wrapped ItemsRequestBytes.
     * @return The CBOR Tag 24 wrapped ReaderAuthenticationBytes.
     */
    fun buildReaderAuthenticationBytes(
        sessionTranscript: ByteArray,
        itemsRequestBytes: ByteArray
    ): ByteArray

    /**
     * Constructs the CBOR Tag 24 wrapped ItemsRequestBytes.
     *
     * @param itemsRequest The [ItemsRequest] domain model.
     * @return The CBOR Tag 24 wrapped ItemsRequestBytes.
     */
    fun buildItemsRequestBytes(itemsRequest: ItemsRequest): ByteArray

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
     * Constructs a [DeviceRequest] from the [itemsRequest] and optional [readerAuth]
     * signature and encodes it to CBOR bytes.
     *
     * @param itemsRequest The [ItemsRequest] domain model.
     * @param itemsRequestBytes The preserved CBOR Tag 24 wrapped ItemsRequestBytes.
     * @param readerAuth The optional ReaderAuthentication signature.
     */
    fun buildDeviceRequest(
        itemsRequest: ItemsRequest,
        itemsRequestBytes: ByteArray? = null,
        readerAuth: ByteArray? = null
    ): ByteArray

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
     * Builds a SessionData message containing only a termination status code (20).
     * Used to signal session end to the holder per ISO 18013-5 §8.3.3.1.3.
     */
    fun buildTerminationSessionData(): ByteArray

    /**
     * Decrypts [deviceResponseBytes] using AES-256-GCM with the [skDevice] session key
     * and decodes the plaintext into a [DeviceResponse] domain model.
     *
     * @throws DecryptDeviceResponseException if decryption or decoding fails.
     */
    @Throws(DecryptDeviceResponseException::class)
    fun decryptDeviceResponse(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        decryptCounter: UInt
    ): DeviceResponse
}
