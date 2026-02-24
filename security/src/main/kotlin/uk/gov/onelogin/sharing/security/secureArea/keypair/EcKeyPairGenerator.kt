package uk.gov.onelogin.sharing.security.secureArea.keypair

import java.security.InvalidAlgorithmParameterException
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.spec.ECGenParameterSpec
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.secureArea.KeyPairGenerator

/**
 * [KeyPairGenerator] implementation that uses Elliptic Curve (EC) cryptography.
 */
class EcKeyPairGenerator(private val logger: Logger) : KeyPairGenerator {
    /**
     * @return a [KeyPair] containing an [java.security.interfaces.ECPrivateKey] and
     * [java.security.interfaces.ECPublicKey]. Returns null if an error occurs.
     */
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair? = try {
        val keyPairGenerator = java.security.KeyPairGenerator.getInstance(algorithm)
        val ecSpec = ECGenParameterSpec(parameterSpec)
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        logger.debug(logTag, "Generated EC key pair: ${keyPair.public}")
        keyPair
    } catch (e: NoSuchAlgorithmException) {
        handleException("No such algorithm exception", e)
    } catch (e: InvalidAlgorithmParameterException) {
        handleException("Invalid algorithm parameter exception", e)
    }

    /**
     * Logs to the class' [logger]. Uses the [throwable]'s [Throwable.message] if it exists.
     * Otherwise, uses [logMessage] as the primary message.
     */
    private fun handleException(logMessage: String, throwable: Throwable): KeyPair? {
        logger.error(
            logTag,
            throwable.message ?: logMessage,
            throwable
        )
        return null
    }
}
