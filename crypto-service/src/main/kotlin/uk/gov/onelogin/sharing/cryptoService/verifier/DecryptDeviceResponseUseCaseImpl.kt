package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

@ContributesBinding(AppScope::class, binding = binding<DecryptDeviceResponseUseCase>())
class DecryptDeviceResponseUseCaseImpl(
    private val sessionEncryption: SessionEncryption,
    private val logger: Logger
) : DecryptDeviceResponseUseCase {

    override fun invoke(
        deviceResponseBytes: ByteArray,
        skDevice: ByteArray,
        encryptCounter: UInt
    ): ByteArray = try {
        logger.debug(logTag, "Decrypting DeviceResponse with counter: $encryptCounter")

        return sessionEncryption.decryptPayload(
            key = skDevice,
            data = deviceResponseBytes,
            role = DeviceRole.HOLDER,
            decryptCounter = encryptCounter
        ).also {
            logger.debug(logTag, LOG_DECRYPT_SUCCESS)
        }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.error(logTag, LOG_DECRYPT_ERROR, e)
        throw DecryptDeviceResponseException(LOG_DECRYPT_ERROR, e)
    }

    companion object {
        const val LOG_DECRYPT_SUCCESS = "DeviceResponse decrypted successfully"
        const val LOG_DECRYPT_ERROR = "Error decrypting DeviceResponse"
    }
}
