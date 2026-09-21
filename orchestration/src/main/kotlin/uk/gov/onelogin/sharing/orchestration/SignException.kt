package uk.gov.onelogin.sharing.orchestration

/**
 * Error contract for [CredentialProvider.sign].
 *
 * Consumers map signing errors onto one of these so the Sharing SDK can react appropriately
 * to the error types:
 *
 * - [LocalAuthCancelled]: the user cancelled or dismissed the local-authentication prompt.
 *   This is a neutral outcome. The sharing session stays active but no progress is made.
 *
 * - [SignError]: signing failed for any reason. This is a fatal outcome. The SDK
 *   terminates the exchange (transmitting an encrypted termination `SessionData`) and the
 *   journey ends in a failed state.
 */
sealed class SignException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {

    /**
     * The user cancelled or dismissed the local-authentication prompt while signing.
     *
     * Neutral outcome: the current sharing session remains active and the signing operation
     * can be retried.
     */
    class LocalAuthCancelled(message: String? = null, cause: Throwable? = null) :
        SignException(message, cause)

    /**
     * Signing failed for a reason other than local-authentication cancellation.
     *
     * Fatal outcome: the sharing session is terminated and the journey ends in a failed state.
     */
    class SignError(message: String? = null, cause: Throwable? = null) :
        SignException(message, cause)
}
