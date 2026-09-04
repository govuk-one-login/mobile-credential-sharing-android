package uk.gov.onelogin.sharing.models.mdoc.exceptions

/**
 * Extendable [Exception] for when an error occurs.
 *
 * Specifically, this means that the User journey should complete / finish due to being unable
 * to continue.
 *
 * DCMAW-21664: Wrap `ReaderAuthCredentialProvider` usages in a try / catch, catching this
 * exception.
 */
open class UnrecoverableError(override val message: String? = null, override val cause: Throwable) :
    Exception(message, cause)
