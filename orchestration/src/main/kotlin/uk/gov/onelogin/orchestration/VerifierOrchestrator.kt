package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
class VerifierOrchestrator(private val logger: Logger) : Orchestrator.Verifier {
    override fun start(requiredPermissions: Set<String>) {
        logger.debug(logTag, START_ORCHESTRATION_SUCCESS)
    }

    override fun cancel() {
        logger.debug(logTag, CANCEL_ORCHESTRATION_SUCCESS)
    }

    override fun reset() {
        logger.debug(
            logTag,
            "Cleared Orchestrator verifier session"
        )
    }
}
