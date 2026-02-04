package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator>())
@Inject
class HolderOrchestrator(private val holderSession: HolderSession) : Orchestrator {

    override fun start() = holderSession.transitionToState("started")

    override fun cancel() = holderSession.transitionToState("cancel")
}

// Session
interface HolderSession {
    fun transitionToState(state: String)
}

@ContributesBinding(scope = AppScope::class, binding = binding<HolderSession>())
@Inject
class HolderSessionImpl(private val logger: Logger) : HolderSession {
    override fun transitionToState(state: String) {
        logger.debug(logTag, "Transitioning to state: $state")
    }
}
