package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.cameraService.scan.OrchestratorInteractor
import uk.gov.onelogin.sharing.core.VerifierUiScope

@ContributesBinding(VerifierUiScope::class, binding = binding<OrchestratorInteractor>())
@Inject
class OrchestratorInteractorImpl(private val orchestrator: Orchestrator.Verifier) :
    OrchestratorInteractor {

    override fun cancel() {
        orchestrator.cancel()
    }
}
