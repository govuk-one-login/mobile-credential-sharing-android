package uk.gov.onelogin.sharing.core

/**
 * Functional interface used to declare an implementation's awareness of whether it's in a completed
 * state.
 */
fun interface Completable {
    /**
     * @return `true` when the implementation considers itself to be complete. Otherwise, `false`.
     */
    fun isComplete(): Boolean
}
