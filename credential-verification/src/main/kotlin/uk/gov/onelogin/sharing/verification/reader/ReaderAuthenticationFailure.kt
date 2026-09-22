package uk.gov.onelogin.sharing.verification.reader

/**
 * Exception thrown when Reader Authentication fails during processing.
 */
class ReaderAuthenticationFailure(
    val reason: ReaderAuthenticationReason,
    cause: Throwable? = null
) : Exception("Reader Authentication failed with reason: $reason", cause)
