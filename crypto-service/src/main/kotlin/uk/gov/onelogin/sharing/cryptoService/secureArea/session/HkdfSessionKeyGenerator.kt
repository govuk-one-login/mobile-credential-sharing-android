package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.generateSalt
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.hkdfKeyGeneration
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

@ContributesBinding(AppScope::class)
class HkdfSessionKeyGenerator(private val logger: Logger) : SessionKeyGenerator {
    /**
     * Generates a single session key from a given shared secret key, a generated cryptographic
     * salt created from the SessionTranscriptBytes and a string containing the
     * corresponding role: "SkReader" and "SkDevice"
     *
     * Session keys are generated deterministically by each party, and used in the subsequent
     * encryption and decryption of messages between devices
     *
     * @return [ByteArray] object representing the session key
     */
    override fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: DeviceRole
    ): ByteArray {
        val salt = generateSalt(sessionTranscriptBytes)
        val roleAsBytes = role.hdkfRoleAsString.toByteArray(Charsets.UTF_8).also {
            logger.debug(
                logTag,
                "Encoded session role as bytes: ${it.decodeToString()}"
            )
        }

        return hkdfKeyGeneration(
            sharedKey,
            salt,
            roleAsBytes
        ).also {
            logger.debug(
                logTag,
                "Generated HKDF session key"
            )
        }
    }
}
