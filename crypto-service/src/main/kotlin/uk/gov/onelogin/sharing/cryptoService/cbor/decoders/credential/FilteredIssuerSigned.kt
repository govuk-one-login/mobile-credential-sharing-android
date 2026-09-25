package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

/**
 * Result of filtering a credential against a DeviceRequest.
 *
 * @param issuerSigned The filtered IssuerSigned, containing only the retained
 * IssuerSignedItemBytes, used to construct the DeviceResponse.
 * @param matchedAttributes The attributes retained per namespace, in the same order as the items
 * in [issuerSigned]. This reflects any `age_over_NN` substitution performed during filtering, so it
 * exactly describes what will be shared with the Verifier.
 */
data class FilteredIssuerSigned(
    val issuerSigned: IssuerSigned,
    val matchedAttributes: Map<String, List<MatchedAttribute>>
)
