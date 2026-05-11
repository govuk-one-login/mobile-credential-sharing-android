package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.io.ByteArrayOutputStream
import java.security.GeneralSecurityException
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential

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
            val toBeSigned = buildCoseSignStructure(deviceAuthenticationBytes)

            val signatureBytes = credentialProvider.sign(
                payload = toBeSigned,
                documentId = validatedCredential.credentialId
            )
            logger.debug(logTag, "Successfully retrieved signature from credential provider")

            val signatureResult = deviceSignatureService.buildDeviceSignedStructures(signatureBytes)
            logger.debug(logTag, "Successfully generated DeviceSigned")

            return DeviceSigned(
                nameSpaces = EMPTY_CBOR_MAP,
                deviceAuth = signatureResult.deviceAuth
            )
        } catch (e: DeviceSignatureException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        } catch (e: GeneralSecurityException) {
            throw DeviceSignatureException("Failed to generate device response", e)
        }
    }

    private companion object {
        /** CBOR encoding of an empty map: major type 5, length 0 */
        val EMPTY_CBOR_MAP = byteArrayOf(0xA0.toByte())

        /** CBOR encoding of {1: -7} — the ES256 algorithm protected header */
        val PROTECTED_HEADER_ES256 = byteArrayOf(0xA1.toByte(), 0x01, 0x26)
    }

    /**
     * Builds the COSE Sig_structure for COSE_Sign1:
     * `["Signature1", protectedHeaders, external_aad, payload]`
     *
     * The protected header contains `{1: -7}` (alg: ES256).
     */
    private fun buildCoseSignStructure(payload: ByteArray): ByteArray {
        return ByteArrayOutputStream().also { out ->
            out.write(0x84) // array(4)
            // "Signature1" - text string (10 bytes)
            out.write(0x6A) // text(10)
            out.write("Signature1".toByteArray())
            // protected header as bstr
            out.write(0x43) // bstr(3)
            out.write(PROTECTED_HEADER_ES256)
            // external_aad: empty bstr
            out.write(0x40) // bstr(0)
            // payload as bstr
            out.write(cborBstrHeader(payload.size))
            out.write(payload)
        }.toByteArray()
    }

    private fun cborBstrHeader(length: Int): ByteArray = when {
        length < 24 -> byteArrayOf((0x40 or length).toByte())
        length < 256 -> byteArrayOf(0x58, length.toByte())
        length < 65536 -> byteArrayOf(0x59, (length shr 8).toByte(), (length and 0xFF).toByte())
        else -> byteArrayOf(
            0x5A,
            (length shr 24).toByte(),
            (length shr 16 and 0xFF).toByte(),
            (length shr 8 and 0xFF).toByte(),
            (length and 0xFF).toByte()
        )
    }
}
