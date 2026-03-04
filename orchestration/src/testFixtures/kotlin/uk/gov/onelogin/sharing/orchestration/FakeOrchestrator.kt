package uk.gov.onelogin.sharing.orchestration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

class FakeOrchestrator(
    initialHolderState: MutableStateFlow<HolderSessionState> = MutableStateFlow(
        HolderSessionState.NotStarted
    )

) : Orchestrator.Holder,
    Orchestrator.Verifier,
    Resettable {

    override val holderSessionState: SharedFlow<HolderSessionState> = initialHolderState

    var startCount = 0
    var cancelCount = 0

    override fun start() {
        startCount++
    }

    override fun cancel() {
        cancelCount++
    }

    override fun reset() {
        startCount = 0
        cancelCount = 0
    }
}
