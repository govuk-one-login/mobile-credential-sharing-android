package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.FilterIssuerSignedUseCase
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.FilteredIssuerSigned
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.NoMatchingAttributesException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned

class FakeFilterIssuerSignedUseCase(
    private val exceptionToThrow: NoMatchingAttributesException? = null,
    private val resultToReturn: FilteredIssuerSigned = FilteredIssuerSigned(
        issuerSigned = SharingIssuerSigned(
            nameSpaces = emptyMap(),
            issuerAuth = byteArrayOf()
        ),
        matchedAttributes = emptyMap()
    )
) : FilterIssuerSignedUseCase {

    var lastValidatedCredential: ParsedRawCredential? = null
    var lastDeviceRequest: DeviceRequest? = null

    override fun filter(
        validatedCredential: ParsedRawCredential,
        deviceRequest: DeviceRequest
    ): FilteredIssuerSigned {
        lastValidatedCredential = validatedCredential
        lastDeviceRequest = deviceRequest
        exceptionToThrow?.let { throw it }
        return resultToReturn
    }
}
