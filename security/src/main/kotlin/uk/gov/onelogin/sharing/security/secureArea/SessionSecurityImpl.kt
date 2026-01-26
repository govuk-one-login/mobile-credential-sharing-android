package uk.gov.onelogin.sharing.security.secureArea

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.java.generateSalt
import uk.gov.onelogin.sharing.security.cryptography.java.hkdfKeyGeneration
import uk.gov.onelogin.sharing.security.cryptography.Constants.EC_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.EC_PARAMETER_SPEC
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyAgreement

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session.
 *
 * This implementation uses Elliptic Curve (EC) cryptography.
 *
 * @param logger An instance of [Logger] for logging events.
 */
@ContributesBinding(ViewModelScope::class)
class SessionSecurityImpl(private val logger: Logger) : SessionSecurity {

    private lateinit var sessionKeyPair: KeyPair

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
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair? = try {
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
        val ecSpec = ECGenParameterSpec(parameterSpec)
        keyPairGenerator.initialize(ecSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        logger.debug(logTag, "Generated EC key pair: ${keyPair.public}")
        sessionKeyPair = keyPair
        keyPair
    } catch (e: NoSuchAlgorithmException) {
        logger.error(logTag, e.message ?: "No such algorithm exception", e)
        null
    } catch (e: InvalidAlgorithmParameterException) {
        logger.error(logTag, e.message ?: "Invalid algorithm parameter exception", e)
        null
    }

    /**
     * Generates a shared secret using the Elliptic Curve Diffie-Hellman (ECDH) key agreement protocol.
     *
     * This method takes the holder's private key and the eReader's public key to compute a
     * common secret that can be used to derive symmetric keys for encrypting communication.
     *
     * @param holderKey The private key of the key holder.
     * @param eReaderKey The public key of the reader.
     * @param logger A [Logger] instance for logging debug information in case of an error.
     * @return A [ByteArray] containing the computed shared secret.
     * @throws InvalidKeyException if the provided keys are invalid or incompatible for ECDH,
     *         wrapping the original exception.
     */
    override fun generateSharedSecret(
        holderKey: ECPrivateKey,
        eReaderKey: ECPublicKey,
        logger: Logger
    ): ByteArray {
        try {
            val keyAgreement = KeyAgreement.getInstance("ECDH")
            keyAgreement.init(holderKey)
            keyAgreement.doPhase(eReaderKey, true)
            return keyAgreement.generateSecret()
        } catch (e: InvalidKeyException) {
            logger.debug(logTag, "Unable to create shared secret (status 10): $e")
            throw InvalidKeyException("Unexpected Exception", e)
        }
    }

    override fun generateSessionPublicKey(): CoseKey {
        generateEcKeyPair(EC_ALGORITHM, EC_PARAMETER_SPEC)

        return CoseKey.generateCoseKey(sessionKeyPair.public as ECPublicKey)
    }

    override fun getSessionPrivateKey(): ECPrivateKey = sessionKeyPair.private as ECPrivateKey

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
        role: String
    ): ByteArray {

        if (role != "SKReader" && role != "SKDevice") {
            val errorMessage = "Invalid role string (status 10) supplied: $role"
            logger.debug(logTag, errorMessage)
            throw InvalidAlgorithmParameterException(errorMessage)
        }

        val salt = generateSalt(sessionTranscriptBytes)
        val roleAsBytes = role.encodeCbor()

        return hkdfKeyGeneration(
            sharedKey,
            salt,
            roleAsBytes
        )
    }
}
