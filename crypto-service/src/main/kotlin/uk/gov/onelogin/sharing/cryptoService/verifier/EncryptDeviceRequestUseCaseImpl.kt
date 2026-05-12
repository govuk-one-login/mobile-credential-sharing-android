package uk.gov.onelogin.sharing.cryptoService.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.createNistInitialisationVector
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

@Inject
@ContributesBinding(AppScope::class, binding = binding<EncryptDeviceRequestUseCase>())
class EncryptDeviceRequestUseCaseImpl(
    private val sessionEncryption: SessionEncryption,
    private val logger: Logger
) : EncryptDeviceRequestUseCase {

    override operator fun invoke(
        deviceRequestBytes: ByteArray,
        skReader: ByteArray,
        encryptCounter: UInt
    ): ByteArray = try {
        logger.debug(logTag, "Encrypting DeviceRequest with counter: $encryptCounter")
        sessionEncryption.encryptPayload(
            key = skReader,
            data = deviceRequestBytes,
            role = DeviceRole.VERIFIER,
            encryptCounter = encryptCounter
        ).also {
            logger.debug(logTag, LOG_ENCRYPT_SUCCESS)
            val nextCounter = encryptCounter + 1u
            val nextIv = createNistInitialisationVector(
                DeviceRole.VERIFIER.nistInitialisationVectorIdentifier,
                nextCounter
            )
            logger.debug(
                logTag,
                "Message counter: ${nextIv.joinToString(" ") { "0x%02x".format(it) }}"
            )
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
