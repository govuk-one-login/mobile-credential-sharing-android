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
     * Processes the scanned Device Engagement data: generates the Verifier's
     * ephemeral key pair and constructs the SessionTranscriptBytes.
     *
     * @param qrCodeData The base64url-encoded Device Engagement string
     *   (with the `mdoc:` prefix already stripped).
     * @return A [ProcessEngagementResult] containing the eReaderKeyTagged and
     *   SessionTranscriptBytes.
     * @throws IllegalArgumentException if [qrCodeData] is blank.
     * @throws IllegalStateException if key pair generation fails.
     */
    fun processEngagement(qrCodeData: String): ProcessEngagementResult
}
