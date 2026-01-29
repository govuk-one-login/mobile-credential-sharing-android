package uk.gov.onelogin.sharing

import uk.gov.onelogin.sharing.core.Receiver


interface Orchestrator: Receiver<OrchestratorEvent> {

    fun start()

    fun cancel()
}


sealed interface OrchestratorEvent {

    sealed interface HolderOrchestratorEvent: OrchestratorEvent {
        data object Start: HolderOrchestratorEvent
        data object Cancel: HolderOrchestratorEvent
    }
}