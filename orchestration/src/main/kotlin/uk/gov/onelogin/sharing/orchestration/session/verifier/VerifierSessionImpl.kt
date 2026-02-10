package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.CANNOT_COMPLETE_TRANSITION
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.cannotFindTransitions
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.cannotTransitionTo

/**
 * Implementation of [VerifierSessionState] that utilises a backing [MutableStateFlow] for the
 * [currentState] property.
 *
 * Internally, the [transitionTo] function uses [update] instead of [MutableStateFlow.emit].
 *
 * @param internalState The [VerifierSessionState] that the [currentState] begins with.
 * Defaults to a [MutableStateFlow] beginning with [VerifierSessionState.NotStarted].
 * @param transitionMap The [Map] of valid transitions. Used within [transitionTo]. Defaults to
 * [validVerifierTransitions].
 */
class VerifierSessionImpl(
    private val logger: Logger,
    private val internalState: MutableStateFlow<VerifierSessionState> =
        MutableStateFlow(VerifierSessionState.NotStarted),
    private val transitionMap: VerifierSessionStateTransitions = validVerifierTransitions
) : VerifierSession {

    override val currentState: StateFlow<VerifierSessionState> = internalState

    override fun transitionTo(state: VerifierSessionState) {
        try {
            val availableTransitions = checkNotNull(
                transitionMap[currentState.value::class]
            ) {
                cannotFindTransitions(currentState.value::class.java.simpleName)
            }

            check(state::class in availableTransitions) {
                cannotTransitionTo(
                    fromStateName = currentState.value::class.java.simpleName,
                    toStateName = state::class.java.simpleName,
                )
            }
        } catch (exception: IllegalStateException) {
            logger.error(
                logTag,
                CANNOT_COMPLETE_TRANSITION,
                exception
            )

            throw exception
        }

        internalState.update { previousState ->
            state.also {
                logger.debug(
                    logTag,
                    "Transitioned from '${previousState::class.java.simpleName}' to " +
                        "'${state::class.java.simpleName}'"
                )
            }
        }
    }

    override fun reset() {
        internalState.update {
            VerifierSessionState.NotStarted.also {
                logger.debug(
                    logTag,
                    "Cleared verifier session state"
                )
            }
        }
    }
}
