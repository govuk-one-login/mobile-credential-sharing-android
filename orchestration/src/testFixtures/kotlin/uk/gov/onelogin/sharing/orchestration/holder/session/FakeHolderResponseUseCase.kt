package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

class FakeHolderResponseUseCase : HolderResponseUseCase {
    var exception: Exception? = null
    var deviceSignedToReturn: DeviceSigned = DeviceSigned(
        nameSpaces = byteArrayOf(),
        deviceAuth = byteArrayOf()
    )
    var lastSelectedCredential: Credential? = null
    var lastDeviceAuthenticationBytes: ByteArray? = null

    override suspend fun generateDeviceResponse(
        selectedCredential: Credential,
        deviceAuthenticationBytes: ByteArray,
        credentialProvider: CredentialProvider
    ): DeviceSigned {
        lastSelectedCredential = selectedCredential
        lastDeviceAuthenticationBytes = deviceAuthenticationBytes
        exception?.let { throw DeviceSignatureException("Sign failed", it) }
        return deviceSignedToReturn
    }
}
