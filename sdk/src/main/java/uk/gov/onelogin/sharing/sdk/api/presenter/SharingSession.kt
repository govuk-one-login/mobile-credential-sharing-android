package uk.gov.onelogin.sharing.sdk.api.presenter

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

/**
 * Represents an active credential sharing session (Holder role).
 *
 * This is the primary public API for consumers adopting the Holder role.
 * The session encapsulates all state and actions for a single sharing journey,
 * and can be used in two modes:
 *
 * **Full UI** — pass the session to [uk.gov.onelogin.sharing.ui.impl.ShareCredential]:
 * ```kotlin
 * ShareCredential(session = session)
 * ```
 *
 * **Headless** — drive the session directly:
 * ```kotlin
 * session.start()
 * session.sessionState.collect { state -> /* render own UI */ }
 * ```
 */
interface SharingSession {
    /**
     * Observable state of the holder session.
     */
    val sessionState: StateFlow<HolderSessionState>

    /**
     * Begins the sharing journey.
     * Triggers prerequisite checks and device engagement.
     */
    fun start()

    /**
     * Cancels the sharing journey.
     * Terminates the BLE session if active.
     */
    fun cancel()

    /**
     * Resets the session to its initial state, allowing it to be reused.
     */
    fun reset()

    /**
     * Confirms user consent to share the requested attributes.
     * Should be called when the user approves the verifier's request.
     */
    fun confirmConsent()

    /**
     * Denies user consent to share the requested attributes.
     * Sends a termination message to the verifier.
     */
    fun denyConsent()
}
