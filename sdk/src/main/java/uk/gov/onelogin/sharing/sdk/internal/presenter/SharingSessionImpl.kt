package uk.gov.onelogin.sharing.sdk.internal.presenter

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.sdk.api.presenter.SharingSession
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

/**
 * Internal implementation of [SharingSession] that wraps the [Orchestrator.Holder].
 *
 * This class is internal to the SDK and not exposed to consumers.
 * It holds the [CredentialSharingAppGraph] reference for creating the UI dependency graph.
 */
class SharingSessionImpl(
    val appGraph: CredentialSharingAppGraph,
    val orchestrator: Orchestrator.Holder
) : SharingSession {

    override val sessionState: StateFlow<HolderSessionState>
        get() = orchestrator.holderSessionState

    override fun start() {
        orchestrator.start()
    }

    override fun cancel() {
        orchestrator.cancel()
    }

    override fun reset() {
        orchestrator.reset()
    }

    override fun confirmConsent() {
        orchestrator.confirmConsent()
    }

    override fun denyConsent() {
        orchestrator.denyConsent()
    }
}
