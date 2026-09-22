package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.security.GeneralSecurityException
import kotlinx.coroutines.CancellationException
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialSigningException
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
        val toBeSigned = try {
            deviceSignatureService.buildCoseSignStructure(deviceAuthenticationBytes)
        } catch (e: GeneralSecurityException) {
            throw DeviceSignatureException("Failed to build COSE sign structure", e)
        }

        val signatureBytes = sign(
            toBeSigned = toBeSigned,
            documentId = validatedCredential.credentialId
        )
        logger.debug(logTag, "Successfully retrieved signature from credential provider")

        return try {
            val signatureResult = deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            logger.debug(logTag, "Successfully generated DeviceSigned")

            SharingDeviceSigned(
                deviceNameSpacesBytes = EMPTY_DEVICE_NAMESPACES,
                deviceSignature = signatureResult.coseSign1Array
            )
        } catch (e: DeviceSignatureException) {
            throw DeviceSignatureException("Failed to build DeviceSigned structure", e)
        } catch (e: GeneralSecurityException) {
            throw DeviceSignatureException("Failed to build DeviceSigned structure", e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun sign(toBeSigned: ByteArray, documentId: String): ByteArray = try {
        credentialProvider.sign(
            payload = toBeSigned,
            documentId = documentId
        )
    } catch (e: CredentialSigningException.Recoverable) {
        logger.debug(logTag, "Recoverable signing failure (e.g. local authentication cancelled)")
        throw e
    } catch (e: CredentialSigningException.Unrecoverable) {
        throw DeviceSignatureException(
            e.message ?: "Fatal signing failure from credential provider",
            e
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw DeviceSignatureException(e.message ?: "Failed to sign device authentication", e)
    }

    private companion object {
        /** CBOR encoding of an empty map: major type 5, length 0 */
        val EMPTY_DEVICE_NAMESPACES = byteArrayOf(0xA0.toByte())
    }
}
