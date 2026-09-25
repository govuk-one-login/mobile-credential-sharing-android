package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

fun interface FilterIssuerSignedUseCase {
    /**
     * Filters the credential's nameSpaces against the DeviceRequest, preserving original
     * IssuerSignedItemBytes to maintain MSO hash integrity.
     *
     * @return a [FilteredIssuerSigned] holding the filtered [IssuerSigned] (used to build the
     * DeviceResponse) and, per namespace, the attributes that were matched (used to display
     * exactly what will be shared on the consent screen).
     * @throws NoMatchingAttributesException if no matching namespaces or attributes are found.
     */
    fun filter(
        validatedCredential: ParsedRawCredential,
        deviceRequest: DeviceRequest
    ): FilteredIssuerSigned
}
