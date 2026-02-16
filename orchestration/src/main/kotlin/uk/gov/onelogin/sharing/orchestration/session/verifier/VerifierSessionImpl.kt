package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.CANNOT_COMPLETE_TRANSITION
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.cannotFindTransitions
import uk.gov.onelogin.sharing.orchestration.session.StateContainer.Transitional.LogMessages.performedTransition

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

    override fun isComplete(): Boolean = currentState.value.isComplete()

    override fun getAvailableTransitions(): Set<KClass<out VerifierSessionState>> =
        checkNotNull(transitionMap[currentState.value::class]) {
            cannotFindTransitions(currentState.value::class.java.simpleName)
        }

    override fun update(state: VerifierSessionState) {
        internalState.update { previousState ->
            state.also {
                logger.debug(
                    logTag,
                    performedTransition(
                        fromStateName = previousState::class.java.simpleName,
                        toStateName = state::class.java.simpleName
                    )
                )
            }
        }
    }

    override fun logError(message: String, throwable: Throwable) {
        logger.error(
            logTag,
            CANNOT_COMPLETE_TRANSITION,
            throwable
        )
    }
}
