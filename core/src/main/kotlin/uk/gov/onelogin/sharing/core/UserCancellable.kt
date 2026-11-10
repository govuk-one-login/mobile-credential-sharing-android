package uk.gov.onelogin.sharing.core

/**
 * Allows consumers to check whether an implementation's state is cancellable by the User via
 * an interaction, such as an Android-powered device's back press.
 *
 * Implementations are often (sealed) classes that represent the User's current progress in an
 * app journey.
 */
interface UserCancellable {

    /**
     * Checks whether Users are capable of cancelling their progress within a given User journey.
     *
     * @return `true` when a User can cancel out of a given journey at this current point in time.
     * Otherwise, return `false`.
     */
    fun userCanCancel(): Boolean

    /**
     * @return `true` when a User should be asked to confirm whether they want to cancel a
     * digital credential verification journey. Otherwise `false`.
     */
    fun shouldConfirmCancellation(): Boolean

    /**
     * Whether the toolbar close button should be hidden for this state.
     *
     * Used when a state can be cancellable (for example via a back press) while still
     * hiding the toolbar close button
     *
     * @return `true` when the toolbar close button should be hidden. Otherwise `false`.
     */
    fun hideCloseButton(): Boolean
}
