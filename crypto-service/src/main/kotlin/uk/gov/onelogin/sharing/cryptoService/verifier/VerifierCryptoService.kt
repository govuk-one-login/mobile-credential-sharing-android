package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException

/**
 * Handles cryptographic operations for the Verifier role.
 *
 * The Orchestrator delegates to this service during the verification lifecycle:
 * 1. [processEngagement] — Decodes the QR code, generates ephemeral keys,
 *    calculates the Session Transcript, and decorates the session's crypto context.
 * 2. [deriveSessionKeys] — Derives the SKReader and SKDevice session keys from
 *    the shared secret and SessionTranscriptBytes.
 */
interface VerifierCryptoService {
    /**
     * Processes the scanned Device Engagement data: generates the Verifier's
     * ephemeral key pair, constructs the SessionTranscriptBytes, and stores
     * the results in the session's crypto context.
     *
     * @param qrCodeData The base64url-encoded Device Engagement string
     *   (with the `mdoc:` prefix already stripped).
     * @param updateContext Callback to decorate the session's crypto context.
     * @throws IllegalArgumentException if [qrCodeData] is blank.
     * @throws IllegalStateException if key pair generation fails.
     */
    fun processEngagement(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    )

    /**
     * Derives the SKReader and SKDevice 32-byte session keys using HKDF-SHA256.
     *
     * @param sharedSecret The shared secret (ZAB) derived via ECKA-DH.
     * @param sessionTranscriptBytes The CBOR Tag 24 wrapped SessionTranscript bytes.
     * @return A [Pair] where [Pair.first] is SKReader and [Pair.second] is SKDevice.
     * @throws SessionKeyDerivationException if either key derivation fails.
     */
    fun deriveSessionKeys(
        sharedSecret: ByteArray,
        sessionTranscriptBytes: ByteArray
    ): Pair<ByteArray, ByteArray>
}
