package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementation of [HolderSessionState] that utilises a backing [MutableStateFlow] for the
 * [currentState] property.
 *
 * Internally, the [transitionTo] function uses [update] instead of [MutableStateFlow.emit].
 *
 * @param internalState The [HolderSessionState] that the [currentState] begins with. Defaults to a
 * [MutableStateFlow] beginning with [HolderSessionState.NotStarted].
 * @param transitionMap The [Map] of valid transitions. Used within [transitionTo]. Defaults to
 * [validHolderTransitions].
 */
class HolderSessionImpl(
    private val internalState: MutableStateFlow<HolderSessionState> =
        MutableStateFlow(HolderSessionState.NotStarted),
    private val transitionMap: HolderSessionStateTransitions = validHolderTransitions,
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
}
