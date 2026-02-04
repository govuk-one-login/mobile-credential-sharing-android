package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.Orchestrator

class FakeOrchestrator : Orchestrator.Holder {
    var startCount = 0
    var cancelCount = 0

    override fun start() {
        startCount++
    }

    override fun cancel() {
        cancelCount++
    }
}
