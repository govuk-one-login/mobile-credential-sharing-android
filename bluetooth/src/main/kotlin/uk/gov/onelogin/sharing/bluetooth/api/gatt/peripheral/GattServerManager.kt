package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import java.util.UUID
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued

interface GattServerManager : AutoCloseable {
    val events: SharedFlow<GattServerEvent>

    fun open(serviceUuid: UUID)

    fun notifySessionEnd(serviceUuid: UUID): SessionEndStateQueued

    /**
     * Sends [data] to the connected Verifier by chunking it into MTU-sized packets and
     * notifying via the Server2Client characteristic.
     *
     * Each intermediate chunk is prefixed with `0x01` and the final chunk with `0x00`.
     *
     * @param serviceUuid The UUID of the active GATT service.
     * @param data The CBOR-encoded SessionData bytes to transmit.
     * @return `true` if all chunks were sent successfully, `false` otherwise.
     */
    fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean
}
