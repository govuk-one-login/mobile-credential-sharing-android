package uk.gov.onelogin.sharing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.OrchestratorEvent.HolderOrchestratorEvent
import uk.gov.onelogin.sharing.core.logger.SystemCrashLogger

class HolderOrchestrator(logger: SystemCrashLogger) : Orchestrator {
    val state: StateFlow<HolderSession> = MutableStateFlow(HolderSession())

    var holderSession: HolderSession = HolderSession()

    override fun start() = receive(HolderOrchestratorEvent.Start)
    override fun cancel() = receive(HolderOrchestratorEvent.Cancel)
    override fun receive(event: OrchestratorEvent) {
        TODO("Not yet implemented")
    }
}

class HolderSession {


}
