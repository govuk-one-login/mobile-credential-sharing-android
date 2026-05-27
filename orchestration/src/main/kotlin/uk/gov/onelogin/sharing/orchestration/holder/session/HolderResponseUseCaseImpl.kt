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
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceSigned

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<HolderResponseUseCase>())
class HolderResponseUseCaseImpl(
    private val logger: Logger,
    private val deviceSignatureService: DeviceSignatureUseCase,
    private val credentialProvider: CredentialProvider
) : HolderResponseUseCase {

    override suspend fun generateDeviceResponse(
        validatedCredential: ValidatedCredential,
        deviceAuthenticationBytes: ByteArray
    ): DeviceSigned {
        try {
            val toBeSigned = deviceSignatureService.buildCoseSignStructure(
                deviceAuthenticationBytes
            )

            val signatureBytes = credentialProvider.sign(
                payload = toBeSigned,
                documentId = validatedCredential.credentialId
            )
            logger.debug(logTag, "Successfully retrieved signature from credential provider")

            val signatureResult = deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            logger.debug(logTag, "Successfully generated DeviceSigned")

            return SharingDeviceSigned(
                deviceNameSpacesBytes = EMPTY_DEVICE_NAMESPACES,
                deviceSignature = signatureResult.coseSign1Array
            )
        } catch (e: DeviceSignatureException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        } catch (e: GeneralSecurityException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        }
    }

    private companion object {
        /** CBOR encoding of an empty map: major type 5, length 0 */
        val EMPTY_DEVICE_NAMESPACES = byteArrayOf(0xA0.toByte())
    }
}
