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
}
