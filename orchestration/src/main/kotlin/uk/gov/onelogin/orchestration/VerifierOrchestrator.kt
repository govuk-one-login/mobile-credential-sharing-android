package uk.gov.onelogin.orchestration

import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

class VerifierOrchestrator(private val logger: Logger) : Orchestrator.Verifier {
    override fun start(requiredPermissions: Set<String>) {
        logger.debug(logTag, "start orchestration")
    }

    override fun cancel() {
        logger.debug(logTag, "cancel orchestration")
    }

    override fun reset() {
        logger.debug(
            logTag,
            "Cleared Orchestrator verifier session"
        )
    }
}
