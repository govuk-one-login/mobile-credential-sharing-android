package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator>())
@Inject
class HolderOrchestrator(private val logger: Logger) : Orchestrator.Holder {

    override fun start() = logger.debug(logTag, "start orchestration")

    override fun cancel() = logger.debug(logTag, "cancel orchestration")
}
