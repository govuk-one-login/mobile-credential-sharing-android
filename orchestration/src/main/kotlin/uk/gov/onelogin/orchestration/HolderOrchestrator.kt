package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Holder>())
class HolderOrchestrator(logger: Logger, session: HolderSession) :
    AbstractOrchestrator<HolderSessionState>(
        logger = logger,
        session = session
    ),
    Orchestrator.Holder {
    override val resetSessionLogMessage: String =
        "Cleared Orchestrator holder session"

    override fun getStartState(requiredPermissions: Set<String>): HolderSessionState =
        HolderSessionState.Preflight(requiredPermissions)

    override fun getCancellationState(): HolderSessionState = HolderSessionState.Complete.Cancelled
}
