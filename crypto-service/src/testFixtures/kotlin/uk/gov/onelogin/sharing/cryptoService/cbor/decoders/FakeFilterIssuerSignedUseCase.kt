package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.FilterIssuerSignedUseCase
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.NoMatchingAttributesException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

class FakeFilterIssuerSignedUseCase(
    private val exceptionToThrow: NoMatchingAttributesException? = null,
    private val issuerSignedToReturn: IssuerSigned = SharingIssuerSigned(
        nameSpaces = emptyMap(),
        issuerAuth = byteArrayOf()
    )
) : FilterIssuerSignedUseCase {

    var lastValidatedCredential: ParsedRawCredential? = null
    var lastDeviceRequest: DeviceRequest? = null

    override fun filter(
        validatedCredential: ParsedRawCredential,
        deviceRequest: DeviceRequest
    ): IssuerSigned {
        lastValidatedCredential = validatedCredential
        lastDeviceRequest = deviceRequest
        exceptionToThrow?.let { throw it }
        return issuerSignedToReturn
    }
}
