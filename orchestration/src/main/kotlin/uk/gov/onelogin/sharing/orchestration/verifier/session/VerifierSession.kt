package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoContext
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Abstraction for containing high-level information about the current position in the User journey
 * for verifying digital credentials with devices containing digital credentials.
 */
interface VerifierSession :
    Completable,
    StateContainer.Complete<VerifierSessionState> {
    val cryptoContext: VerifierCryptoContext?

    fun updateCryptoContext(update: (VerifierCryptoContext?) -> VerifierCryptoContext)
}
