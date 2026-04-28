package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

interface ConfirmConsentUseCase {
    @Throws(DeviceSignatureException::class)
    suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        credentialProvider: CredentialProvider
    ): DeviceSigned
}
