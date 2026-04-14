package uk.gov.onelogin.sharing.cryptoService.verifier

import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException

/**
 * Handles cryptographic operations for the Verifier role.
 *
 * The Orchestrator delegates to this service during the verification lifecycle:
 * 1. [establishSession] — Decodes the QR code, generates ephemeral keys,
 *    calculates the Session Transcript, computes the shared secret, and derives
 *    the SKReader and SKDevice session keys.
 */
fun interface VerifierCryptoService {
    /**
     * Processes the scanned Device Engagement data: generates the Verifier's
     * ephemeral key pair, constructs the SessionTranscriptBytes, computes the
     * shared secret (ZAB), and derives the session keys.
     *
     * @param qrCodeData The base64url-encoded Device Engagement string
     *   (with the `mdoc:` prefix already stripped).
     * @param updateContext Callback to decorate the session's crypto context.
     * @throws IllegalArgumentException if [qrCodeData] is blank.
     * @throws IllegalStateException if key pair generation fails.
     * @throws SharedSecretException.IncompatibleCurve if EDeviceKey.Pub is not on P-256.
     * @throws SharedSecretException.MalformedKey if EDeviceKey.Pub is malformed.
     * @throws SessionKeyDerivationException if either session key derivation fails.
     */
    fun establishSession(
        qrCodeData: String,
        updateContext: (VerifierCryptoContext) -> VerifierCryptoContext
    )
}
