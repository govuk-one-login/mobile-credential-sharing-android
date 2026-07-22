package uk.gov.onelogin.sharing.core

/**
 * Allows consumers to check whether an implementation's state is cancellable by the User via
 * an interaction, such as an Android-powered device's back press.
 *
 * Implementations are often (sealed) classes that represent the User's current progress in an
 * app journey.
 */
fun interface UserCancellable {

    /**
     * Checks whether Users are capable of cancelling their progress within a given User journey.
     *
     * @return `true` when a User can cancel out of a given journey at this current point in time.
     * Otherwise, return `false`.
     */
    fun userCanCancel(): Boolean
}
