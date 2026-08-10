package uk.gov.onelogin.sharing.sdk

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.sdk.api.presenter.SharingSession

class FakeSharingSession(
    private val orchestrator: Orchestrator.Holder
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
