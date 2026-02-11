package uk.gov.onelogin.orchestration

import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Abstract [Orchestrator] implementation that handles common business logic for all
 * leaf implementations.
 *
 * The [State] type represents what the [session] state machine wraps around.
 */
abstract class AbstractOrchestrator<State : Any>(
    private val logger: Logger,
    private val session: StateContainer.Complete<State>
) : Orchestrator {

    /**
     * The [String] to use when successfully resetting the [session].
     */
    protected abstract val resetSessionLogMessage: String

    /**
     * @return A [State] instance designating the beginning of the User journey.
     * @see uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight
     * @see uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Preflight
     */
    protected abstract fun getStartState(requiredPermissions: Set<String>): State

    /**
     * @return a [State] instance designating the User choosing to prematurely end the journey.
     * @see uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled
     * @see uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Complete.Cancelled
     */
    protected abstract fun getCancellationState(): State

    override fun start(requiredPermissions: Set<String>) {
        try {
            session.transitionTo(getStartState(requiredPermissions))
            logger.debug(logTag, START_ORCHESTRATION_SUCCESS)
        } catch (exception: IllegalStateException) {
            START_ORCHESTRATION_ERROR.let { logMessage ->
                logger.error(
                    logTag,
                    logMessage,
                    OrchestratorCannotStartException(logMessage, exception)
                )
            }
        }
    }

    override fun cancel() {
        try {
            session.transitionTo(getCancellationState())
            logger.debug(logTag, CANCEL_ORCHESTRATION_SUCCESS)
        } catch (exception: IllegalStateException) {
            CANCEL_ORCHESTRATION_ERROR.let { logMessage ->
                logger.error(
                    logTag,
                    logMessage,
                    OrchestratorCannotCancelException(logMessage, exception)
                )
            }
        }
    }

    override fun reset() {
        session.reset().also {
            logger.debug(
                logTag,
                resetSessionLogMessage
            )
        }
    }
}
