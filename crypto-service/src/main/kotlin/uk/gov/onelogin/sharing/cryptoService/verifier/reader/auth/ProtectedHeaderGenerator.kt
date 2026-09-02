package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import javax.security.cert.Certificate

fun interface ProtectedHeaderGenerator {
    fun generateProtectedHeaders(leafCertificate: Certificate): Map<UInt, Any>
}

