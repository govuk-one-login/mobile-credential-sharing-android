package uk.gov.onelogin.sharing.cryptoService.holder

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

/**
 * Handles cryptographic operations for the Holder role.
 */
fun interface HolderCryptoService {
    /**
     * Constructs and CBOR-encodes a SessionData termination message.
     *
     * @param status The termination status code.
     * @return The CBOR-encoded SessionData bytes ready for transmission.
     */
    fun buildTerminationSessionData(status: SessionDataStatus): ByteArray
}
