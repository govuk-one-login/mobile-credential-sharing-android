package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSession
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
class VerifierOrchestrator(logger: Logger, session: VerifierSession) :
    AbstractOrchestrator<VerifierSessionState>(
        logger = logger,
        session = session
    ),
    Orchestrator.Verifier {
    override val resetSessionLogMessage: String =
        "Cleared Orchestrator verifier session"

    override fun getStartState(requiredPermissions: Set<String>): VerifierSessionState =
        VerifierSessionState.Preflight(requiredPermissions)

    override fun getCancellationState(): VerifierSessionState =
        VerifierSessionState.Complete.Cancelled
}
