package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

class FakeConfirmConsentUseCase : ConfirmConsentUseCase {
    var exception: Exception? = null
    var deviceSignedToReturn: DeviceSigned = DeviceSigned(
        nameSpaces = byteArrayOf(),
        deviceAuth = byteArrayOf()
    )

    override suspend fun execute(
        sessionTranscript: ByteArray,
        deviceRequest: DeviceRequest,
        credentialProvider: CredentialProvider
    ): DeviceSigned {
        exception?.let { throw DeviceSignatureException("Sign failed", it) }
        return deviceSignedToReturn
    }
}
