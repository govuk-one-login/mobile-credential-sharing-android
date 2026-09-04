package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

/**
 * Component for providing protected headers for COSE_Sign1 signatures.
 *
 * @sample CoseSign1UnprotectedHeaderGenerator
 */
fun interface UnprotectedHeaderGenerator {
    /**
     * @return a [Pair] of entries. The first entry is the data for the unprotected headers. The
     * second entry is a [ByteArray] representation of the first entry.
     * Usually, this [ByteArray] will be CBOR-encoded.
     *
     * @sample CoseSign1UnprotectedHeaderGenerator.generateUnprotectedHeaders
     */
    fun generateUnprotectedHeaders(
        certificateChain: List<Certificate>
    ): Pair<Map<Long, Any>, ByteArray>

    companion object {
        internal const val UNPROTECTED_HEADER_X5_CHAIN = 33L
    }
}
