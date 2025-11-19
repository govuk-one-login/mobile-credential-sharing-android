package uk.gov.onelogin.sharing.verifier.scan.state.permission

import kotlinx.coroutines.flow.StateFlow

sealed interface PreviouslyDeniedPermissionState {
    /**
     * Combines functionality of the [State] and [Updater] interfaces.
     *
     * @see State
     * @see Updater
     */
    interface Complete :
        State,
        Updater

    /**
     * Interface for exposing a [Boolean] [StateFlow]. Commonly paired with the [Updater]
     * interface.
     *
     * @see Updater
     */
    interface State {
        val hasPreviouslyDeniedPermission: StateFlow<Boolean>
    }

    /**
     * Interface for updating a [Boolean] object. Commonly paired with the [State] interface.
     *
     * @see State
     */
    fun interface Updater {
        fun update(hasPreviouslyDeniedPermission: Boolean)
    }
}
