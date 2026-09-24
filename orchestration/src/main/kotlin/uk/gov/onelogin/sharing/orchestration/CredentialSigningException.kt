package uk.gov.onelogin.sharing.orchestration

/**
 * Error contract for [CredentialProvider.sign].
 *
 * A consuming application maps its own signing errors onto one of these outcomes so
 * the Sharing SDK can react appropriately. The SDK recognises exactly two outcomes and does not
 * depend on any consumer-specific error types.
 *
 * - [Recoverable]: signing did not complete but the sharing session can continue.
 *   For example the user cancelling or dismissing the local-authentication prompt.
 *
 * - [Unrecoverable]: signing failed for a reason the session cannot recover from.
 */
sealed class CredentialSigningException(cause: Throwable? = null) : Exception(cause) {

    /**
     * Signing did not complete but the sharing session can continue and be retried.
     *
     * Neutral outcome: the current sharing session remains active and the signing operation can be
     * retried.
     */
    class Recoverable(cause: Throwable? = null) : CredentialSigningException(cause)

    /**
     * Signing failed for a reason the session cannot recover from.
     *
     * Fatal outcome: the sharing session is terminated and the journey ends in a failed state.
     */
    class Unrecoverable(cause: Throwable? = null) : CredentialSigningException(cause)
}
