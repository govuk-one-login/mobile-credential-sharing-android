package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.Orchestrator

class FakeOrchestrator :
    Orchestrator.Holder,
    Orchestrator.Verifier {
    var startCount = 0
    var cancelCount = 0

    override fun start(requiredPermissions: Set<String>) {
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
