package uk.gov.onelogin.sharing.orchestration.session

import kotlinx.coroutines.flow.StateFlow

/**
 * Declares that an implementation exposes [State] objects via a kotlin [kotlinx.coroutines.flow.StateFlow].
 */
interface StateContainer<State : Any> {
    val currentState: StateFlow<State>

    /**
     * Functional interface that allows implementations to transition to different [State]s.
     *
     * Most commonly implemented alongside the [StateContainer] interface.
     */
    fun interface Transitional<State : Any> {
        /**
         * Updates the internal state based on validations.
         *
         * Implementations usually update the [StateContainer.currentState] property.
         *
         * @throws IllegalStateException when the provided [state] cannot be transitioned to.
         */
        @Throws(IllegalStateException::class)
        fun transitionTo(state: State)

        data object LogMessages {
            @JvmStatic
            fun cannotFindTransitions(stateName: String): String =
                "Cannot find applicable transitions for current state: $stateName"

            @JvmStatic
            fun cannotTransitionTo(
                fromStateName: String,
                toStateName: String,
            ): String = "Current state ($fromStateName) cannot transition to: $toStateName"
        }
    }
}
