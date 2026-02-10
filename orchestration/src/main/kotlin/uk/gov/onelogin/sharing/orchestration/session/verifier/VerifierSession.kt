package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.session.StateTransitional
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState

/**
 * Abstraction for containing high-level information about the current position in the User journey
 * for verifying digital credentials with devices containing digital credentials.
 */
interface VerifierSession : Resettable, StateTransitional<VerifierSessionState> {
    /**
     * The current position of the User within the User journey.
     */
    val currentState: StateFlow<HolderSessionState>
}
