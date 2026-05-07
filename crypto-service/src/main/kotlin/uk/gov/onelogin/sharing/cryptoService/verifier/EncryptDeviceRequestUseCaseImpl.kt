package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

@Inject
@ContributesBinding(AppScope::class, binding = binding<EncryptDeviceRequestUseCase>())
class EncryptDeviceRequestUseCaseImpl(
    private val sessionEncryption: SessionEncryption,
    private val logger: Logger
) : EncryptDeviceRequestUseCase {

    override fun encrypt(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray = try {
        sessionEncryption.encryptPayload(
            key = skReader,
            data = deviceRequestBytes,
            role = DeviceRole.VERIFIER,
            encryptCounter = encryptCounter
        ).also {
            logger.debug(logTag, LOG_ENCRYPT_SUCCESS)
            logger.debug(logTag, "Message counter: ${encryptCounter + 1u}")
        }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.error(logTag, LOG_ENCRYPT_ERROR, e)
        throw EncryptDeviceRequestException(LOG_ENCRYPT_ERROR, e)
    }

    internal companion object {
        const val LOG_ENCRYPT_SUCCESS = "DeviceRequest encrypted successfully"
        const val LOG_ENCRYPT_ERROR = "Error encrypting DeviceRequest"
    }
}
