package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * Determines the [InboundMessageType] of raw BLE message bytes before full decryption.
 *
 * Classifies whether the inbound bytes are a [SessionEstablishment][InboundMessageType.SessionEstablishment],
 * a status-only [SessionData][InboundMessageType.StatusOnly] (peer termination), or an
 * [unrecognised][InboundMessageType.Unknown] message that constitutes a sequencing violation.
 */
fun interface InboundMessageClassifier {
    /**
     * Determines the type of the inbound message.
     */
    fun getMessageType(rawBytes: ByteArray): InboundMessageType
}
