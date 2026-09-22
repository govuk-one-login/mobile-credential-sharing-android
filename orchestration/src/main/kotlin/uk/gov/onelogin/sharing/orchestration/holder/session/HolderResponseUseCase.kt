package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

fun interface HolderResponseUseCase {
    /**
     * Builds the signed [DeviceSigned] structure for the response.
     *
     * @throws CredentialSigningException.Recoverable when signing did not complete but the session
     * can continue (e.g. the user cancelled the local-authentication prompt). This is a neutral
     * outcome the caller must not treat as fatal.
     * @throws DeviceSignatureException when signing fails for any other reason (a fatal outcome).
     */
    @Throws(CredentialSigningException.Recoverable::class, DeviceSignatureException::class)
    suspend fun generateDeviceResponse(
        validatedCredential: ValidatedCredential,
        deviceAuthenticationBytes: ByteArray
    ): DeviceSigned
}
