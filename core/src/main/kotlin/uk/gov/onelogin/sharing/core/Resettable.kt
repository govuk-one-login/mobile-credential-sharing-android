package uk.gov.onelogin.sharing.core

/**
 * Implementations marked with this functional interface have internal state that need removing
 * at the end of a User journey.
 *
 * Examples of this include in-memory state for implementations.
 */
fun interface Resettable {
    fun reset()
}
