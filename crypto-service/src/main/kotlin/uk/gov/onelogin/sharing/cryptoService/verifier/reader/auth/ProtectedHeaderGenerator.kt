package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

fun interface ProtectedHeaderGenerator {
    fun generateProtectedHeaders(leafCertificate: Certificate): Map<UInt, Any>
}

