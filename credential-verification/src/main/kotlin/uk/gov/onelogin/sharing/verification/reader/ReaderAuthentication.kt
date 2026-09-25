package uk.gov.onelogin.sharing.verification.reader

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

/**
 * High-level component contract for Reader Authentication orchestration.
 *
 * Authenticates candidate document requests, verifies candidate signatures
 * and privacy policy metadata, and selects the first passing candidate.
 */
fun interface ReaderAuthentication {
    /**
     * Authenticates the DeviceRequest candidates and selects the first candidate
     * passing verification.
     *
     * @param deviceRequest The decoded [DeviceRequest] received over BLE.
     * @param untaggedSessionTranscriptBytes The active untagged session transcript bytes.
     * @param supportedDocumentTypes The list of document types supported by the product.
     * @return [ReaderAuthenticationOutcome.Success] or [ReaderAuthenticationOutcome.Unfulfillable].
     * @throws ReaderAuthenticationFailure if every candidate fails verification.
     */
    fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>
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
