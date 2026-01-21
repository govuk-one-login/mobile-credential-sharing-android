package uk.gov.onelogin.sharing.security.cbor.decoders

/**
 * Functional interface for obtaining the Session Transcript.
 * This is based off of the proceeding inputs:
 *
 */
fun interface SessionTranscriptDecoder {
    /**
     * Creates a [ByteArray] that's used as a session transcript.
     *
     * Note that there's currently no parameter for 'Handover' as it's always null.
     *
     * @param cborBase64Url The encoded Device Engagement String, obtained via scanning a valid
     * QR code.
     * @param sessionEstablishmentBytes The Session Establishment byte array.
     *
     * @return An [uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor.encoded] [ByteArray]
     */
    fun deriveSessionTranscript(
        cborBase64Url: String,
        sessionEstablishmentBytes: ByteArray
    ): ByteArray
}
