package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

/**
 * Component for providing protected headers for COSE_Sign1 signatures.
 *
 * @sample CoseSign1ProtectedHeaders
 */
fun interface ProtectedHeaderGenerator {
    /**
     * @return a [Pair] of entries. The first entry is the data for the protected headers. The
     * second entry is a [ByteArray] representation of the first entry.
     * Usually, this [ByteArray] will be CBOR-encoded.
     *
     * @sample CoseSign1ProtectedHeaders.generateProtectedHeaders
     */
    fun generateProtectedHeaders(leafCertificate: Certificate): Pair<Map<Long, Any>, ByteArray>

    companion object {
        internal const val PROTECTED_HEADER_X5T: Long = 34L
        internal const val PROTECTED_HEADER_ALGORITHM: Long = 1L
        internal const val PROTECTED_HEADER_VALUE_SHA256: Int = -16
    }
}

