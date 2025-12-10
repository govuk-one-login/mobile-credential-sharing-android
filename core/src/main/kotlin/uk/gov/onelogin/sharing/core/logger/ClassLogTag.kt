package uk.gov.onelogin.sharing.core.logger

/**
 * An extension property on [Any] that provides a logging tag.
 *
 * The tag is derived from the simple name of the class, which is used to
 * identifying the source of log messages.
 */
val Any.logTag: String
    get() = this.javaClass.simpleName
