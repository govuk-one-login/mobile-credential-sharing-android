package uk.gov.onelogin.sharing.orchestration.holder.credential

/**
 * Exception thrown when credential request or validation fails,
 * triggering the 'No Match' Termination Sequence.
 */
class CredentialRequestException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)