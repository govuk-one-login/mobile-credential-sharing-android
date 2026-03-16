package uk.gov.onelogin.sharing.orchestration.verifier.session

import kotlin.reflect.KClass
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Implementation of [VerifierSessionState] that utilises a backing [kotlinx.coroutines.flow.MutableStateFlow] for the
 * [currentState] property.
 *
 * Internally, the [transitionTo] function uses [update] instead of [kotlinx.coroutines.flow.MutableStateFlow.emit].
 *
 * @param internalState The [VerifierSessionState] that the session begins with.
 * Defaults to a [kotlinx.coroutines.flow.MutableStateFlow] beginning with [VerifierSessionState.NotStarted].
 * @param transitionMap The [Map] of valid transitions. Used within [transitionTo]. Defaults to
 * [validVerifierTransitions].
 */
data class VerifierSessionImpl(
    private val logger: Logger,
    private var internalState: VerifierSessionState = VerifierSessionState.NotStarted,
    private val transitionMap: VerifierSessionStateTransitions = validVerifierTransitions
) : VerifierSession,
    Completable by internalState {

    override fun getCurrentState(): VerifierSessionState = internalState

    override fun getAvailableTransitions(): Set<KClass<out VerifierSessionState>> =
        checkNotNull(transitionMap[internalState::class]) {
            StateContainer.Transitional.LogMessages.cannotFindTransitions(
                internalState::class.java.simpleName
            )
        }

    override fun update(state: VerifierSessionState) {
        val previousState = internalState
        internalState = state
        logger.debug(
            logTag,
            StateContainer.Transitional.LogMessages.performedTransition(
                fromStateName = previousState::class.java.simpleName,
                toStateName = state::class.java.simpleName
            )
        )
    }

    override fun logError(message: String, throwable: Throwable) {
        logger.error(
            logTag,
            StateContainer.Transitional.LogMessages.CANNOT_COMPLETE_TRANSITION,
            throwable
        )
    }
}
