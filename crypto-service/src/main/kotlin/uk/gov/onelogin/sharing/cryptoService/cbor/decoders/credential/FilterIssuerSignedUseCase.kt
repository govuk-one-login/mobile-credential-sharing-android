package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned

fun interface FilterIssuerSignedUseCase {
    /**
     * Filters the credential's nameSpaces against the DeviceRequest, preserving original
     * IssuerSignedItemBytes to maintain MSO hash integrity.
     *
     * @throws NoMatchingAttributesException if no matching namespaces or attributes are found.
     */
    fun filter(validatedCredential: ParsedRawCredential, deviceRequest: DeviceRequest): IssuerSigned
}
