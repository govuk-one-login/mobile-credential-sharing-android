package uk.gov.onelogin.sharing.verification.reader

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest

/**
 * Public contract for verifying Reader Authentication cryptographic signatures and certificates.
 */
fun interface VerifyReaderAuthUseCase {
    /**
     * Verifies the Reader Authentication structure for a candidate [DocRequest].
     *
     * @param candidateDocRequest The decoded document request containing preserved rawReaderAuth and itemsRequestBytes.
     * @param untaggedSessionTranscriptBytes The exact active untagged session transcript bytes.
     * @param trustedReaderCertificates The list of trusted Reader CA certificates.
     * @return [VerifiedReaderRequest] on successful verification.
     * @throws ReaderAuthenticationFailure if verification fails.
     */
    fun verify(
        candidateDocRequest: DocRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        trustedReaderCertificates: List<X509Certificate>,
    ): VerifiedReaderRequest
}
