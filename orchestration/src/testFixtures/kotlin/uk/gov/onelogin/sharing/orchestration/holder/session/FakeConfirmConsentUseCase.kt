package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

class FakeConfirmConsentUseCase(
    private val exception: Exception? = null,
    private val documentToReturn: Document = Document(
        docType = "",
        issuerSigned = IssuerSigned(nameSpaces = emptyMap(), issuerAuth = byteArrayOf()),
        deviceSigned = SharingDeviceSigned(deviceNameSpacesBytes = byteArrayOf(), deviceSignature = byteArrayOf())
    )
) : ConfirmConsentUseCase {

    override suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): Document {
        exception?.let { throw DeviceSignatureException("Sign failed", it) }
        return documentToReturn
    }
}
