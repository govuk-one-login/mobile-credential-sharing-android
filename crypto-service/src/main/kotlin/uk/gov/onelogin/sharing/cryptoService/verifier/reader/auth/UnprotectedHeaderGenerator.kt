package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import javax.security.cert.Certificate

fun interface UnprotectedHeaderGenerator {
    fun generateUnprotectedHeaders(certificateChain: List<Certificate>): Map<UInt, Any>
}
