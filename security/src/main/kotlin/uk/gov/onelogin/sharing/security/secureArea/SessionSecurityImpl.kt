package uk.gov.onelogin.sharing.security.secureArea

import java.security.InvalidAlgorithmParameterException
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session.
 *
 * This implementation uses Elliptic Curve (EC) cryptography.
 *
 * @param logger An instance of [Logger] for logging events.
 */
class SessionSecurityImpl(private val logger: Logger) : SessionSecurity {

    /**
     * Generates a new, ephemeral Elliptic Curve (EC) key pair and returns the public key.
     *
     * This public key is intended to be shared with the verifier application as part of the
     * device engagement process. It uses the `secp256r1` curve, which is standard for mDoc/mDL
     * engagement.
     *
     * @return A [PublicKey] object representing the public part of the generated EC key pair,
     * or `null` if the key generation fails.
     */
    override fun generateEcPublicKey(algorithm: String, parameterSpec: String): ECPublicKey? = try {
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        val ecSpec = ECGenParameterSpec(parameterSpec)
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        logger.debug(logTag, "Generated public key: ${keyPair.public}")
        keyPair.public as ECPublicKey
    } catch (e: NoSuchAlgorithmException) {
        logger.error(logTag, e.message ?: "No such algorithm exception", e)
        null
    } catch (e: InvalidAlgorithmParameterException) {
        logger.error(logTag, e.message ?: "Invalid algorithm parameter exception", e)
        null
    }
}
