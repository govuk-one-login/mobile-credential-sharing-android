package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.createSessionResetMessage
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.recreateSessionOnStartMessage
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSession
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
class VerifierOrchestrator(
    private val logger: Logger,
    private val sessionFactory: SessionFactory<VerifierSession>
) : Orchestrator.Verifier {

    private var session: VerifierSession = sessionFactory.create()

    override fun start(requiredPermissions: Set<String>) {
        if (session.isComplete()) {
            session = sessionFactory.create().also {
                logger.debug(
                    logTag,
                    recreateSessionOnStartMessage(Orchestrator.Verifier.JOURNEY_NAME)
                )
            }
        }

        try {
            session.transitionTo(
                VerifierSessionState.Preflight(requiredPermissions)
            )
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
            session.transitionTo(
                VerifierSessionState.Complete.Cancelled
            )
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
        session = sessionFactory.create().also {
            logger.debug(
                logTag,
                createSessionResetMessage(Orchestrator.Verifier.JOURNEY_NAME)
            )
        }
    }
}
