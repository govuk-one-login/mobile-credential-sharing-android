package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.orchestration.SignException
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

fun interface HolderResponseUseCase {
    /**
     * Builds the signed [DeviceSigned] structure for the response.
     *
     * @throws SignException.LocalAuthCancelled when the user cancelled the local-authentication
     * prompt while signing. This is a neutral outcome the caller must not treat as fatal.
     * @throws DeviceSignatureException when signing fails for any other reason (a fatal outcome).
     */
    @Throws(SignException.LocalAuthCancelled::class, DeviceSignatureException::class)
    suspend fun generateDeviceResponse(
        validatedCredential: ValidatedCredential,
        deviceAuthenticationBytes: ByteArray
    ): DeviceSigned
}
