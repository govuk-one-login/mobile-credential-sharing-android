package uk.gov.onelogin.sharing.cryptoService.verifier

/**
 * Handles cryptographic operations for the Verifier role.
 *
 * The Orchestrator delegates to this service during the verification lifecycle:
 * 1. [processEngagement] — Decodes the QR code, generates ephemeral keys,
 *    calculates the Session Transcript, and decorates the session's crypto context.
 */
fun interface VerifierCryptoService {
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
}
