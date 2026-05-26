package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocumentWithPresentation
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class FakeConfirmConsentUseCase(
    private val exception: Exception? = null,
    private val documentToReturn: VerifiableDocument.WithPresentation =
        SharingVerifiableDocumentWithPresentation(
            docType = "",
            issuerSigned = SharingIssuerSigned(
                nameSpaces = emptyMap(),
                issuerAuth = byteArrayOf()
            ),
            deviceSigned = SharingDeviceSigned(
                deviceNameSpacesBytes = byteArrayOf(),
                deviceSignature = byteArrayOf()
            )
        ),
) : ConfirmConsentUseCase {

    override suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned,
    ): VerifiableDocument.WithPresentation {
        exception?.let { throw DeviceSignatureException("Sign failed", it) }
        return documentToReturn
    }
}
