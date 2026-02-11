package uk.gov.onelogin.sharing.orchestration.session

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.CANNOT_COMPLETE_TRANSITION
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.cannotTransitionTo

/**
 * Declares that an implementation exposes [State] objects via a kotlin [StateFlow].
 */
interface StateContainer<State : Any> {
    val currentState: StateFlow<State>

    /**
     * Interface that contains all of the [StateContainer] sub-interfaces.
     *
     * Also provides the [transitionTo] function that consumers use for updating the
     * [StateContainer.currentState] property.
     */
    interface Complete<State : Any> :
        StateContainer<State>,
        Resettable,
        Transitional<State> {
        /**
         * Logs the provided [message] and [throwable].
         *
         * Implementations usually defer to a [uk.gov.logging.api.Logger] instance.
         */
        fun logError(message: String, throwable: Throwable)

        /**
         * Validates then updates the [StateContainer.currentState] to [state].
         *
         * Implementations usually update the [StateContainer.currentState] property.
         *
         * @throws IllegalStateException when the provided [state] cannot be transitioned to.
         */
        @Throws(IllegalStateException::class)
        fun transitionTo(state: State) {
            try {
                val availableTransitions: Set<KClass<out State>> = getAvailableTransitions()

                check(state::class in availableTransitions) {
                    cannotTransitionTo(
                        fromStateName = currentState.value::class.java.simpleName,
                        toStateName = state::class.java.simpleName
                    )
                }
            } catch (exception: IllegalStateException) {
                logError(
                    CANNOT_COMPLETE_TRANSITION,
                    exception
                )

                throw exception
            }

            update(state)
        }
    }

    /**
     * Functional interface that allows implementations to transition to different [State]s.
     *
     * Most commonly implemented alongside the [StateContainer] interface.
     */
    interface Transitional<State : Any> {
        /**
         * @return A [Set] of applicable [State] classes that can be transitioned towards.
         * @throws IllegalStateException when there are no available transitions available.
         */
        @Throws(IllegalStateException::class)
        fun getAvailableTransitions(): Set<KClass<out State>>

        /**
         * Updates the internal state.
         *
         * Note that no validations occur when calling this function.
         *
         * Implementations usually update the [StateContainer.currentState] property.
         */
        fun update(state: State)

        /**
         * Data object for containing logging messages applicable to [StateContainer] and it's
         * sub-interfaces.
         */
        data object LogMessages {
            const val CANNOT_COMPLETE_TRANSITION: String = "Cannot complete transition"

            @JvmStatic
            fun cannotFindTransitions(stateName: String): String =
                "Cannot find applicable transitions for current state: $stateName"

            @JvmStatic
            fun cannotTransitionTo(fromStateName: String, toStateName: String): String =
                "Current state ($fromStateName) cannot transition to: $toStateName"

            @JvmStatic
            fun performedTransition(fromStateName: String, toStateName: String): String =
                "Transitioned from '$fromStateName' to '$toStateName'"
        }
    }
}
