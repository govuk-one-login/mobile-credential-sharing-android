package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

fun interface ConfirmConsentUseCase {
    @Throws(DeviceSignatureException::class)
    suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        validatedCredential: ValidatedCredential,
        filteredIssuerSigned: IssuerSigned
    ): Document
}
