package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator>())
@Inject
class HolderOrchestratorImpl(logger: Logger) : HolderOrchestrator {

    private val session: Session = HolderSession(logger)

    override fun start() = session.transitionToState("started")

    override fun cancel() = session.transitionToState("cancel")
}
