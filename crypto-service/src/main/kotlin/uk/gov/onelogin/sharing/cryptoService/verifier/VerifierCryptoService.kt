package uk.gov.onelogin.sharing.cryptoService.verifier

/**
 * Handles cryptographic operations for the Verifier role.
 *
 * The Orchestrator delegates to this service during the verification lifecycle:
 * 1. [processEngagement] — Decodes the QR code, generates ephemeral keys,
 *    calculates the Session Transcript, and populates the session.
 */
fun interface VerifierCryptoService {
    /**
     * Processes the scanned Device Engagement data.
     *
     * Decodes and validates the engagement structure, generates the Verifier's
     * ephemeral key pair, and derives the Session Transcript. The results are
     * stored in the session's crypto context.
     *
     * @param qrCodeData The base64url-encoded Device Engagement string
     *   (with the `mdoc:` prefix already stripped).
     */
    fun processEngagement(qrCodeData: String)
}
