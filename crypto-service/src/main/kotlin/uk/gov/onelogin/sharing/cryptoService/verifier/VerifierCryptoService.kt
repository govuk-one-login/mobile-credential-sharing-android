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
     * Processes the scanned Device Engagement data and constructs the
     * SessionTranscriptBytes.
     *
     * Assembles the SessionTranscript array as defined in ISO 18013-5 section 12.6.1:
     * `[DeviceEngagementBytes, EReaderKeyBytes, Handover]` where Handover is null
     * for QR engagement. The array is then CBOR encoded and wrapped in CBOR Tag 24.
     *
     * @param qrCodeData The base64url-encoded Device Engagement string
     *   (with the `mdoc:` prefix already stripped).
     * @param eReaderKeyTagged The Verifier's ephemeral public key in COSE key format,
     *   wrapped in CBOR Tag 24.
     * @return The SessionTranscriptBytes (CBOR Tag 24 wrapped SessionTranscript).
     * @throws IllegalArgumentException if [qrCodeData] is blank or [eReaderKeyTagged]
     *   is not a valid Tag 24 structure.
     */
    fun processEngagement(qrCodeData: String, eReaderKeyTagged: ByteArray): ByteArray
}
