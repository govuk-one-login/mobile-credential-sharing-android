package uk.gov.onelogin.orchestration

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator>())
@Inject
class HolderOrchestrator(private val logger: Logger) : Orchestrator.Holder {

    /**
     * The [HolderSession] that acts as a state machine for the [HolderOrchestrator].
     *
     * Due to initialisation occurring within the class instead of being composed via constructor
     * injection, it's annotated with [VisibleForTesting] for state verification purposes.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var session: HolderSession = HolderSessionImpl(
        logger = logger
    )

    override fun start(requiredPermissions: Set<String>) {
        try {
            session.transitionTo(
                HolderSessionState.Preflight(requiredPermissions)
            )
            logger.debug(logTag, "start orchestration")
        } catch (exception: IllegalStateException) {
            "Cannot start orchestration".let { logMessage ->
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
            session.transitionTo(HolderSessionState.Complete.Cancelled)
            logger.debug(logTag, "cancel orchestration")
        } catch (exception: IllegalStateException) {
            "Cannot cancel orchestration".let { logMessage ->
                logger.error(
                    logTag,
                    logMessage,
                    OrchestratorCannotCancelException(logMessage, exception)
                )
            }
        }
    }

    override fun reset() {
        update(HolderSessionImpl(logger = logger)).also {
            logger.debug(
                logTag,
                "Cleared Orchestrator holder session"
            )
        }
    }

    /**
     * Convenience function for testing purposes that passes in a pre-configured [session].
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun update(session: HolderSession) {
        this.session = session
    }
}
