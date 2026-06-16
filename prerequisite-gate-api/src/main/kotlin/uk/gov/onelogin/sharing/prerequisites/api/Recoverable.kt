package uk.gov.onelogin.sharing.prerequisites.api

/**
 * Checks whether a User action can resolve a discovered issue in the current User journey.
 *
 * Commonly paired with [Actionable.getAction] after receiving `true` from [isRecoverable].
 */
fun interface Recoverable {
    fun isRecoverable(): Boolean
}
