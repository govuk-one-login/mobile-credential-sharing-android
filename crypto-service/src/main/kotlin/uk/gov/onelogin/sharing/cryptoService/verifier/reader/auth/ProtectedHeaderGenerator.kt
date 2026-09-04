package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

fun interface ProtectedHeaderGenerator {
    fun generateProtectedHeaders(leafCertificate: Certificate): Pair<Map<Long, Any>, ByteArray>

    companion object {
        internal const val PROTECTED_HEADER_X5T: Long = 34L
        internal const val PROTECTED_HEADER_ALGORITHM: Long = 1L
        internal const val PROTECTED_HEADER_VALUE_SHA256: Int = -16
    }
}

