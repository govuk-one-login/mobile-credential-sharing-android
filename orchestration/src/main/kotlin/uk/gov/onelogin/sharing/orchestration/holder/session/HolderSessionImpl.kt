package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.reflect.KClass
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Implementation of [HolderSessionState] that utilises a backing [kotlinx.coroutines.flow.MutableStateFlow] for the
 * [currentState] property.
 *
 * Internally, the [transitionTo] function uses [update] instead of [kotlinx.coroutines.flow.MutableStateFlow.emit].
 *
 * @param internalState The [HolderSessionState] that the session begins with. Defaults to a
 * [kotlinx.coroutines.flow.MutableStateFlow] beginning with [HolderSessionState.NotStarted].
 * @param transitionMap The [Map] of valid transitions. Used within [transitionTo]. Defaults to
 * [validHolderTransitions].
 */
data class HolderSessionImpl(
    private val logger: Logger,
    override val sessionContext: HolderSessionContext,
    private var internalState: HolderSessionState = HolderSessionState.NotStarted,
    private val transitionMap: HolderSessionStateTransitions = validHolderTransitions
) : HolderSession,
    Completable by internalState {

    override fun getCurrentState(): HolderSessionState = internalState

    override fun getAvailableTransitions(): Set<KClass<out HolderSessionState>> =
        checkNotNull(transitionMap[internalState::class]) {
            StateContainer.Transitional.LogMessages.cannotFindTransitions(
                internalState::class.java.simpleName
            )
        }

    override fun update(state: HolderSessionState) {
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
