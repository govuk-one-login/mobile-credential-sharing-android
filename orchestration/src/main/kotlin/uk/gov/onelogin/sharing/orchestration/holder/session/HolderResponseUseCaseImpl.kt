package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.security.GeneralSecurityException
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<HolderResponseUseCase>())
class HolderResponseUseCaseImpl(
    private val logger: Logger,
    private val deviceSignatureService: DeviceSignatureUseCase
) : HolderResponseUseCase {

    override suspend fun generateDeviceResponse(
        selectedCredential: Credential,
        deviceAuthenticationBytes: ByteArray,
        credentialProvider: CredentialProvider
    ): DeviceSigned {
        try {
            val signatureBytes = credentialProvider.sign(
                payload = deviceAuthenticationBytes,
                documentId = selectedCredential.id
            )
            logger.debug(logTag, "Successfully retrieved signature from credential provider")

            val signatureResult = deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            logger.debug(logTag, "Successfully generated DeviceSigned")

            return DeviceSigned(
                nameSpaces = signatureResult.deviceSigned,
                deviceAuth = signatureResult.deviceAuth
            )
        } catch (e: DeviceSignatureException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        } catch (e: GeneralSecurityException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        }
    }
}
