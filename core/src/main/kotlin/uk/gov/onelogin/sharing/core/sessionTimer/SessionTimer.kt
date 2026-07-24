package uk.gov.onelogin.sharing.core.sessionTimer

import kotlin.time.Duration

/**
 * A reusable timer for managing session inactivity.
 */
interface SessionTimer {
    /**
     * Starts the timer with a provided timeout action.
     * If the timer is already running, it will be reset.
     *
     * @param duration The inactivity period before timeout.
     * @param onTimeout The action to perform when the timer expires.
     */
    fun start(duration: Duration, onTimeout: suspend () -> Unit)

    /**
     * Resets the timer to its initial duration.
     * Only has an effect if the timer has been started.
     */
    fun reset()

    /**
     * Stops the timer and cancels any pending timeout actions.
     */
    fun stop()
}
