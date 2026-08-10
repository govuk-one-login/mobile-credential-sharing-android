package uk.gov.onelogin.sharing.sdk

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession

class FakeVerificationSession(
    private val orchestrator: Orchestrator.Verifier
) : VerificationSession {

    override val sessionState: StateFlow<VerifierSessionState>
        get() = orchestrator.verifierSessionState

    override fun start() {
        orchestrator.start()
    }

    override fun cancel() {
        orchestrator.cancel()
    }

    override fun reset() {
        orchestrator.reset()
    }

    override suspend fun processQrCode(qrCode: String?) {
        orchestrator.processQrCode(qrCode)
    }
}
