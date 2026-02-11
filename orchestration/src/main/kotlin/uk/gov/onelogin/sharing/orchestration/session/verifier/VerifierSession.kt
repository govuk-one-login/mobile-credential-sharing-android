package uk.gov.onelogin.sharing.orchestration.session.verifier

import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Abstraction for containing high-level information about the current position in the User journey
 * for verifying digital credentials with devices containing digital credentials.
 */
interface VerifierSession : StateContainer.Complete<VerifierSessionState>
