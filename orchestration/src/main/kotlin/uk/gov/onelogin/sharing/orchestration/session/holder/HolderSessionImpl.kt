package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionImpl.Companion.validTransitions

/**
 * Implementation of [HolderSessionState] that utilises a backing [MutableStateFlow] for the
 * [currentState] property.
 *
 * @param initialState The [HolderSessionState] that the [currentState] begins with.
 * @param validTransitions The [Map] of valid transitions. Used within [transitionTo].
 */
class HolderSessionImpl(
    private val internalState: MutableStateFlow<HolderSessionState> =
        MutableStateFlow(HolderSessionState.NotStarted),
    private val transitionMap: HolderSessionStateTransitions = validTransitions,
) : HolderSession {

    override val currentState: StateFlow<HolderSessionState> = internalState

    override fun transitionTo(state: HolderSessionState) {
        val availableTransitions = checkNotNull(transitionMap[currentState.value::class]) {
            "Cannot find applicable transitions for current state: " +
                currentState.value::class.java.simpleName
        }

        check(state::class in availableTransitions) {
            "Current state (${currentState.value::class.java.simpleName}) cannot transition to: " +
                state::class.java.simpleName
        }

        internalState.update { state }
    }

    companion object {
        @JvmStatic
        val validTransitions: HolderSessionStateTransitions =
            mapOf(
                HolderSessionState.NotStarted::class to setOf(
                    HolderSessionState.Initialising::class,
                    HolderSessionState.Complete.Cancelled::class,
                ),
                HolderSessionState.Initialising::class to setOf(),
            )
    }
}