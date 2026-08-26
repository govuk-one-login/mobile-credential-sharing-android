package uk.gov.onelogin.sharing.orchestration.exceptions

/**
 * Extendable [Exception] for when an error occurs.
 *
 * Specifically, this means that the User is able to reattempt the previous action again.
 */
open class RecoverableError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(cause)
