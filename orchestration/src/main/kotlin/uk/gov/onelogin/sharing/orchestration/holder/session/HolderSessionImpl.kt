package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.logging.api.v2.Logger
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
class HolderSessionImpl(
    private val logger: Logger,
    initialContext: HolderSessionContext,
    private val internalState: MutableStateFlow<HolderSessionState> =
        MutableStateFlow(HolderSessionState.NotStarted),
    private val transitionMap: HolderSessionStateTransitions = validHolderTransitions
) : HolderSession {

    private var _sessionContext = initialContext
    override val sessionContext: HolderSessionContext
        get() = _sessionContext

    override fun updateSessionContext(update: (HolderSessionContext) -> HolderSessionContext) {
        _sessionContext = update(_sessionContext)
    }

    override val currentState: StateFlow<HolderSessionState> = internalState

    override fun isComplete(): Boolean = currentState.value.isComplete()

    override fun getAvailableTransitions(): Set<KClass<out HolderSessionState>> =
        checkNotNull(transitionMap[currentState.value::class]) {
            StateContainer.Transitional.LogMessages.cannotFindTransitions(
                currentState.value::class.java.simpleName
            )
        }

    override fun update(state: HolderSessionState) {
        internalState.update { previousState ->
            state.also {
                logger.debug(
                    logTag,
                    StateContainer.Transitional.LogMessages.performedTransition(
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
            StateContainer.Transitional.LogMessages.CANNOT_COMPLETE_TRANSITION,
            throwable
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HolderSessionImpl

        if (internalState.value != other.internalState.value) return false
        if (transitionMap != other.transitionMap) return false
        if (_sessionContext != other._sessionContext) return false

        return true
    }

    override fun hashCode(): Int {
        var result = internalState.value.hashCode()
        result = 31 * result + transitionMap.hashCode()
        result = 31 * result + _sessionContext.hashCode()
        return result
    }
}
