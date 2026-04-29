package uk.gov.onelogin.sharing.orchestration.holder.credential

/**
 * Handles requesting credentials from the Host App and validating the returned
 * credential's docType against the DeviceRequest.
 */
fun interface CredentialRequestHandler {
    /**
     * Fetches a credential from the Host App for the given [requestedDocType],
     * parses the raw CBOR, extracts the MSO docType, and validates it matches.
     *
     * @throws CredentialRequestException on any failure (host error, empty response,
     *         CBOR parse failure, or docType mismatch).
     */
    suspend fun requestAndValidate(requestedDocType: String): ValidatedCredential
}
