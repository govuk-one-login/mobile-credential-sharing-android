package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Abstraction for containing high-level information about the current position in the User journey
 * for sharing digital credentials with verifying devices.
 */
interface HolderSession :
    Completable,
    StateContainer.Complete<HolderSessionState> {
    val sessionContext: HolderSessionContext

    fun updateSessionContext(update: (HolderSessionContext) -> HolderSessionContext)

    /**
     * Updates the [HolderSessionState.PresentingEngagement.showQr] presentation hint in place when
     * the current state is [HolderSessionState.PresentingEngagement]. This is not a state
     * transition: the state class is unchanged, so it is applied via the non-validating
     * [StateContainer.Transitional.update] rather than [StateContainer.Complete.transitionTo].
     * No-op in any other state.
     */
    fun updateEngagementDisplay(showQr: Boolean) {
        val current = currentState.value
        if (current is HolderSessionState.PresentingEngagement && current.showQr != showQr) {
            update(current.copy(showQr = showQr))
        }
    }
}
