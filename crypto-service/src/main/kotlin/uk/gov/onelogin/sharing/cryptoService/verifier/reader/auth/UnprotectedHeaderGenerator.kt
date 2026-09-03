package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

fun interface UnprotectedHeaderGenerator {
    fun generateUnprotectedHeaders(certificateChain: List<Certificate>): ByteArray

    companion object {
        internal const val UNPROTECTED_HEADER_X5_CHAIN = 33L
    }
}
