package uk.gov.onelogin.sharing.orchestration.holder.session

import java.util.UUID

/**
 * Manages the ISO 18013-5 session termination protocol for the holder.
 *
 * The caller is responsible for sending the SessionData (status 20) before
 * invoking [terminate].
 */
fun interface HolderSessionTerminator {
    /**
     * Executes the termination protocol.
     *
     * @param serviceUuid The active GATT service UUID.
     */
    suspend fun terminate(serviceUuid: UUID)
}
