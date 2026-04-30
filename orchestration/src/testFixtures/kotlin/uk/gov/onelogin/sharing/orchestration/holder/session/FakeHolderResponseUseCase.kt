package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

class FakeHolderResponseUseCase(
    private val exception: Exception? = null,
    private val deviceSignedToReturn: DeviceSigned = DeviceSigned(
        nameSpaces = byteArrayOf(),
        deviceAuth = byteArrayOf()
    )
) : HolderResponseUseCase {
    var lastValidatedCredential: ValidatedCredential? = null
    var lastDeviceAuthenticationBytes: ByteArray? = null

    override suspend fun generateDeviceResponse(
        validatedCredential: ValidatedCredential,
        deviceAuthenticationBytes: ByteArray
    ): DeviceSigned {
        lastValidatedCredential = validatedCredential
        lastDeviceAuthenticationBytes = deviceAuthenticationBytes
        exception?.let { throw DeviceSignatureException("Sign failed", it) }
        return deviceSignedToReturn
    }
}
