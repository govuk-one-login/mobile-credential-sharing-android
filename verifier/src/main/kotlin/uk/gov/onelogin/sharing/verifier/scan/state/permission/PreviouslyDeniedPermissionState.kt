package uk.gov.onelogin.sharing.verifier.scan.state.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

sealed interface PreviouslyDeniedPermissionState {
    /**
     * Combines functionality of the [State] and [Updater] interfaces.
     *
     * @see State
     * @see Updater
     */
    interface Complete :
        State,
        Updater {
        companion object {
            @JvmStatic
            fun from(flow: MutableStateFlow<Boolean>) = object : Complete {
                override val hasPreviouslyDeniedPermission: StateFlow<Boolean>
                    get() = flow

                override fun update(hasPreviouslyDeniedPermission: Boolean) {
                    flow.update { hasPreviouslyDeniedPermission }
                }
            }
        }
    }

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
