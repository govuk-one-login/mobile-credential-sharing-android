package uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral

import java.util.UUID

/**
 * Sends data to the connected remote device over BLE.
 */
fun interface MessageSender {
    /**
     * Sends [data] to the connected device, chunked according to the negotiated MTU.
     *
     * @param serviceUuid The UUID of the active GATT service.
     * @param data The CBOR-encoded SessionData bytes to transmit.
     * @return `true` if all chunks were sent successfully, `false` otherwise.
     */
    fun sendMessage(serviceUuid: UUID, data: ByteArray): Boolean
}
