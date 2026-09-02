package uk.gov.onelogin.sharing.orchestration.exceptions

/**
 * Extendable [Exception] for when an error occurs.
 *
 * Specifically, this means that the User journey should complete / finish due to being unable
 * to continue.
 */
open class UnrecoverableError(override val message: String? = null, override val cause: Throwable) :
    Exception(message, cause)
