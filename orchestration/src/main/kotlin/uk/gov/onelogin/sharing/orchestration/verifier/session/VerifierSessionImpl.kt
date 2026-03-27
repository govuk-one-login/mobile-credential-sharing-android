package uk.gov.onelogin.sharing.orchestration.verifier.session

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import uk.gov.logging.api.v2.Logger
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
class VerifierSessionImpl(
    private val logger: Logger,
    private val internalState: MutableStateFlow<VerifierSessionState> =
        MutableStateFlow(VerifierSessionState.NotStarted),
    private val transitionMap: VerifierSessionStateTransitions = validVerifierTransitions
) : VerifierSession {

    private var _cryptoContext = VerifierCryptoContext()
    override val cryptoContext: VerifierCryptoContext
        get() = _cryptoContext

    override fun updateCryptoContext(update: (VerifierCryptoContext) -> VerifierCryptoContext) {
        _cryptoContext = update(_cryptoContext)
    }

    override val currentState: StateFlow<VerifierSessionState> = internalState

    override fun isComplete(): Boolean = currentState.value.isComplete()

    override fun getAvailableTransitions(): Set<KClass<out VerifierSessionState>> =
        checkNotNull(transitionMap[currentState.value::class]) {
            StateContainer.Transitional.LogMessages.cannotFindTransitions(
                currentState.value::class.java.simpleName
            )
        }

    override fun update(state: VerifierSessionState) {
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

        other as VerifierSessionImpl

        if (internalState.value != other.internalState.value) return false
        if (transitionMap != other.transitionMap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = internalState.value.hashCode()
        result = 31 * result + transitionMap.hashCode()
        return result
    }
}
