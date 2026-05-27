package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

class FakeCredentialRequestHandler : CredentialRequestHandler {
    var resultToReturn: ValidatedCredential? = null
    var exceptionToThrow: CredentialRequestException? = null
    var filteredIssuerSignedToReturn: IssuerSigned = SharingIssuerSigned(
        nameSpaces = emptyMap(),
        issuerAuth = byteArrayOf()
    )

    override suspend fun requestAndValidate(
        requestedDocType: String,
        deviceRequest: DeviceRequest
    ): CredentialRequestResult {
        exceptionToThrow?.let { throw it }
        val validated = resultToReturn
            ?: throw CredentialRequestException("No result configured")
        return CredentialRequestResult(
            validatedCredential = validated,
            filteredIssuerSigned = filteredIssuerSignedToReturn
        )
    }
}
