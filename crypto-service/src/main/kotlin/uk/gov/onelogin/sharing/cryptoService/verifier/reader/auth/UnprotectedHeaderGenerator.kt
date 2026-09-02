package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

fun interface UnprotectedHeaderGenerator {
    fun generateUnprotectedHeaders(certificateChain: List<Certificate>): Map<UInt, Any>
}
