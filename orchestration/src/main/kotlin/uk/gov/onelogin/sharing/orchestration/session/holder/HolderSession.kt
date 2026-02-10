package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.session.StateTransitional

/**
 * Abstraction for containing high-level information about the current position in the User journey
 * for sharing digital credentials with verifying devices.
 */
interface HolderSession : Resettable, StateTransitional<HolderSessionState> {
    /**
     * The current position of the User within the User journey.
     */
    val currentState: StateFlow<HolderSessionState>
}
