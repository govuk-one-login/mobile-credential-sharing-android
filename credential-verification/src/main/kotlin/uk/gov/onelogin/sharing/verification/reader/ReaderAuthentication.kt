package uk.gov.onelogin.sharing.verification.reader

import java.security.cert.X509Certificate

/**
 * High-level component contract for Reader Authentication orchestration.
 *
 * Authenticates decrypted DeviceRequest bytes, filters supported candidate document types,
 * verifies candidate signatures and privacy policy metadata, and selects the first passing candidate.
 */
fun interface ReaderAuthentication {
    /**
     * Authenticates the decrypted DeviceRequest bytes and selects the first candidate
     * passing verification.
     *
     * @param decryptedDeviceRequestBytes The complete plaintext request bytes received over BLE.
     * @param untaggedSessionTranscriptBytes The active untagged session transcript bytes.
     * @param supportedDocumentTypes The list of document types supported by the product.
     * @param trustedReaderCertificates The non-empty list of trusted Reader CA certificates.
     * @return [ReaderAuthenticationOutcome.Success] or [ReaderAuthenticationOutcome.Unfulfillable].
     * @throws ReaderAuthenticationFailure if every candidate fails verification or decoding fails.
     */
    fun authenticateDeviceRequest(
        decryptedDeviceRequestBytes: ByteArray,
        untaggedSessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>,
        trustedReaderCertificates: List<X509Certificate>,
    ): ReaderAuthenticationOutcome
}

/**
 * Outcome of Reader Authentication processing.
 */
sealed interface ReaderAuthenticationOutcome {
    /** Candidate passed authentication and was selected. */
    data class Success(val authenticatedReaderRequest: AuthenticatedReaderRequest) :
        ReaderAuthenticationOutcome

    /** No candidate matched product-supported document types. */
    data object Unfulfillable : ReaderAuthenticationOutcome
}
