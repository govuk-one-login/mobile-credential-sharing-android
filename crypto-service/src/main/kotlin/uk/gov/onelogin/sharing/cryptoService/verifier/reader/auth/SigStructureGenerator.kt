package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

interface SigStructureGenerator {
    fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray
    ): ByteArray
}
