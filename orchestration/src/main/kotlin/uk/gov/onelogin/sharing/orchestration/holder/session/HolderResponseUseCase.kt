package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

interface HolderResponseUseCase {
    @Throws(DeviceSignatureException::class)
    suspend fun generateDeviceResponse(
        selectedCredential: Credential,
        deviceAuthenticationBytes: ByteArray,
        credentialProvider: CredentialProvider
    ): DeviceSigned
}