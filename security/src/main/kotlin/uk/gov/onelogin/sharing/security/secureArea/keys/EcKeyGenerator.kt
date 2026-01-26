package uk.gov.onelogin.sharing.security.secureArea.keys

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cose.CoseKey
import java.security.InvalidAlgorithmParameterException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

/**
 * [KeyGenerator.Complete] implementation that uses Elliptic Curve (EC) cryptography.
 *
 * Also implements the [KeyGenerator.PublicKeyGenerator] and [KeyGenerator.PrivateKeyGenerator]
 * as the class internally stores the last generated [sessionKeyPair].
 */
@ContributesBinding(
    ViewModelScope::class,
    binding = binding<KeyGenerator.KeyPairGenerator>()
)
class EcKeyGenerator(private val logger: Logger) :
    KeyGenerator.Complete,
    KeyGenerator.KeyPairGenerator,
    KeyGenerator.PrivateKeyGenerator,
    KeyGenerator.PublicKeyGenerator {

    /**
     * The last successfully generated [java.security.KeyPair].
     */
    private lateinit var sessionKeyPair: KeyPair

    override fun generateEcKeyPair(
        algorithm: String,
        parameterSpec: String
    ): KeyPair? = try {
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        val ecSpec = ECGenParameterSpec(parameterSpec)
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        logger.debug(logTag, "Generated EC key pair: ${keyPair.public}")
        sessionKeyPair = keyPair
        keyPair
    } catch (e: NoSuchAlgorithmException) {
        handleException("No such algorithm exception", e)
    } catch (e: InvalidAlgorithmParameterException) {
        handleException("Invalid algorithm parameter exception", e)
    }

    override fun generateSessionPublicKey(): CoseKey = sessionKeyPair
        .let { it.public as ECPublicKey }
        .let(CoseKey::generateCoseKey)

    override fun getSessionPrivateKey(): ECPrivateKey =
        sessionKeyPair.private as ECPrivateKey

    private fun handleException(
        logMessage: String,
        throwable: Throwable
    ): KeyPair? {
        logger.error(
            logTag,
            throwable.message ?: logMessage,
            throwable
        )
        return null
    }
}