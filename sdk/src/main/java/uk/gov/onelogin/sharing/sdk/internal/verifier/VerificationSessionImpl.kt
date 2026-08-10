package uk.gov.onelogin.sharing.sdk.internal.verifier

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession

/**
 * Internal implementation of [VerificationSession] that wraps the [Orchestrator.Verifier].
 *
 * This class is internal to the SDK and not exposed to consumers.
 * It holds the [CredentialSharingAppGraph] reference for creating the UI dependency graph.
 */
class VerificationSessionImpl(
    val appGraph: CredentialSharingAppGraph,
    val orchestrator: Orchestrator.Verifier
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
