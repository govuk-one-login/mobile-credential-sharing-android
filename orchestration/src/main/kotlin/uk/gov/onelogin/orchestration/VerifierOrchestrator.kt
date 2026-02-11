package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSession
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
class VerifierOrchestrator(
    private val logger: Logger,
    private val session: VerifierSession,
) : Orchestrator.Verifier {
    override fun start(requiredPermissions: Set<String>) {
        try {
            session.transitionTo(VerifierSessionState.Preflight(requiredPermissions))
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
        session.transitionTo(VerifierSessionState.Complete.Cancelled)
        logger.debug(logTag, CANCEL_ORCHESTRATION_SUCCESS)
    }

    override fun reset() {
        logger.debug(
            logTag,
            "Cleared Orchestrator verifier session"
        )
    }
}
