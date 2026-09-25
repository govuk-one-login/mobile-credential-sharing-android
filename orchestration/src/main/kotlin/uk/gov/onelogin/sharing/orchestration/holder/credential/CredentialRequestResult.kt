package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.MatchedAttribute
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

/**
 * The outcome of fetching, validating and filtering a credential against a DeviceRequest.
 *
 * @param validatedCredential The parsed and docType-validated credential.
 * @param filteredIssuerSigned The filtered IssuerSigned used to construct the DeviceResponse.
 * @param matchedAttributes The attributes matched per namespace. Describes exactly what
 * will be shared with the Verifier.
 */
data class CredentialRequestResult(
    val validatedCredential: ValidatedCredential,
    val filteredIssuerSigned: IssuerSigned,
    val matchedAttributes: Map<String, List<MatchedAttribute>>
)
