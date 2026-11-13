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
     * Updates the [EngagementPresentation] of a [HolderSessionState.PresentingEngagement] state in
     * place, without a validated state transition. No-op in any other state, or when [transform]
     * returns an equal presentation.
     */
    fun updateEngagementPresentation(
        transform: (EngagementPresentation) -> EngagementPresentation
    ) {
        val current = currentState.value as? HolderSessionState.PresentingEngagement ?: return
        val updated = transform(current.presentation)
        if (updated != current.presentation) {
            update(current.copy(presentation = updated))
        }
    }
}
