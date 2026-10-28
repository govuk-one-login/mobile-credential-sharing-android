package uk.gov.onelogin.sharing.bluetooth.api.core

/**
 * Sends data to the connected remote device over BLE.
 */
fun interface MessageSender {
    /**
     * Sends [data] to the connected device, chunked according to the negotiated MTU.
     *
     * The active GATT service is the one established during the connection (open/connect),
     * so it does not need to be supplied per call.
     *
     * @param data The CBOR-encoded SessionData bytes to transmit.
     * @return `true` if all chunks were sent successfully, `false` otherwise.
     */
    suspend fun sendMessage(data: ByteArray): Boolean
}
