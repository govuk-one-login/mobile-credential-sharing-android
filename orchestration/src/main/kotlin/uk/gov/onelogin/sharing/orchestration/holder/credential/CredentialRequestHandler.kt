package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

/**
 * Handles requesting credentials from the Host App, validating the returned
 * credential's docType against the DeviceRequest, and filtering the IssuerSigned
 * items to only those requested by the Verifier.
 */
fun interface CredentialRequestHandler {
    /**
     * Fetches a credential from the Host App for the given [requestedDocType],
     * parses the raw CBOR, validates the docType matches, and filters the
     * IssuerSigned items against the [deviceRequest].
     *
     * @throws CredentialRequestException on any failure (host error, empty response,
     *         CBOR parse failure, docType mismatch, or no matching attributes).
     */
    suspend fun requestAndValidate(
        requestedDocType: String,
        deviceRequest: DeviceRequest
    ): CredentialRequestResult
}
