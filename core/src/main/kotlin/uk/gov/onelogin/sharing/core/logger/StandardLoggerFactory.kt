package uk.gov.onelogin.sharing.core.logger

import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger

/**
 * Provides a console-based implementation of the [Logger] interface.
 * This implementation is platform-agnostic and suitable for use in non-Android modules
 * or for testing purposes.
 */
object StandardLoggerFactory {
    /**
     * Creates and returns a [Logger] instance.
     *
     * @return A configured [Logger].
     */
    fun create(): Logger = SystemLogger()
}

/**
 * An extension property on [Any] that provides a logging tag.
 *
 * The tag is derived from the simple name of the class, which is used to
 * identifying the source of log messages.
 */
val Any.logTag: String
    get() = this.javaClass.simpleName
