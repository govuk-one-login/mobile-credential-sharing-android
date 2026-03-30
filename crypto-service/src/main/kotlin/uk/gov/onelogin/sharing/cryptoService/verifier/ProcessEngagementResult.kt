package uk.gov.onelogin.sharing.cryptoService.verifier

/**
 * The result of processing a Device Engagement QR code.
 *
 * @param eReaderKeyTagged The Verifier's ephemeral public key (CBOR Tag 24 wrapped COSE key).
 * @param sessionTranscriptBytes The SessionTranscriptBytes (#6.24 wrapped SessionTranscript).
 */
data class ProcessEngagementResult(
    val eReaderKeyTagged: ByteArray,
    val sessionTranscriptBytes: ByteArray
)
