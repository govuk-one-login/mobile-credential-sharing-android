package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import java.security.cert.Certificate

/**
 * Component for providing `Sig_Structure` data for use in COSE_Sign1 signatures.
 *
 * @sample CoseSigStructureGenerator
 * @sample SigningSignatureStructure
 */
fun interface SigStructureGenerator {
    /**
     * @return A [ByteArray] structure containing the relevant signature to use within a
     * `Cose_Sign1` signature.
     *
     * @sample CoseSigStructureGenerator.generateSignatureStructure
     * @sample SigningSignatureStructure.generateSignatureStructure
     */
    fun generateSignatureStructure(
        certificateChain: List<Certificate>,
        readerAuthenticationPayload: ByteArray,
    ): ByteArray
}

