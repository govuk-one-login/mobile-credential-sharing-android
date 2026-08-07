package uk.gov.onelogin.sharing.sdk.api.verifier

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

/**
 * Represents an active credential verification session (Verifier role).
 *
 * This is the primary public API for consumers adopting the Verifier role.
 * The session encapsulates all state and actions for a single verification journey,
 * and can be used in two modes:
 *
 * **Full UI** — pass the session to [uk.gov.onelogin.sharing.ui.impl.VerifyCredential]:
 * ```kotlin
 * VerifyCredential(session = session)
 * ```
 *
 * **Headless** — drive the session directly:
 * ```kotlin
 * session.start()
 * session.sessionState.collect { state -> /* render own UI */ }
 * ```
 */
interface VerificationSession {
    /**
     * Observable state of the verifier session.
     */
    val sessionState: StateFlow<VerifierSessionState>

    /**
     * Begins the verification journey.
     * Triggers prerequisite checks and prepares for QR scanning.
     */
    fun start()

    /**
     * Cancels the verification journey.
     * Terminates the BLE session if active.
     */
    fun cancel()

    /**
     * Resets the session to its initial state, allowing it to be reused.
     */
    fun reset()

    /**
     * Processes a scanned QR code from the holder's device engagement.
     *
     * @param qrCode The scanned QR code content, or null if scanning failed.
     */
    suspend fun processQrCode(qrCode: String?)
}
