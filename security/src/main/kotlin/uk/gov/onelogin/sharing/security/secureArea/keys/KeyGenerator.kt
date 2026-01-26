package uk.gov.onelogin.sharing.security.secureArea.keys

import uk.gov.onelogin.sharing.security.cose.CoseKey
import java.security.KeyPair
import java.security.interfaces.ECPrivateKey

/**
 * Collection of interfaces that expose specific cryptographic behaviours.
 */
sealed interface KeyGenerator {
    /**
     * Wrapper interface for implementing the complete suite of [KeyGenerator]s.
     *
     * @see KeyPairGenerator
     * @see PrivateKeyGenerator
     * @see PublicKeyGenerator
     */
    interface Complete : KeyPairGenerator, PrivateKeyGenerator, PublicKeyGenerator

    /**
     * Creates [java.security.KeyPair] instances for use in the code base.
     */
    fun interface KeyPairGenerator {
        /**
         * Generates a new, ephemeral Elliptic Curve (EC) key pair.
         *
         * The public key is intended to be shared with the verifier application as part of the
         * device engagement process.
         *
         * @return A [java.security.KeyPair] object containing a
         * [java.security.interfaces.ECPublicKey] and [java.security.interfaces.ECPrivateKey].
         * Returns null when an error occurs.
         */
        fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair?
    }

    /**
     * Provides [java.security.PublicKey] instances in the form of
     * [uk.gov.onelogin.sharing.security.cose.CoseKey] data structures.
     *
     * Most commonly defers to a [KeyPairGenerator] implementation when obtaining the
     * [java.security.PublicKey].
     */
    fun interface PublicKeyGenerator {
        fun generateSessionPublicKey(): CoseKey
    }

    /**
     * Provides [java.security.interfaces.ECPrivateKey] instances.
     *
     * Most commonly defers to a [KeyPairGenerator] implementation.
     */
    fun interface PrivateKeyGenerator {
        fun getSessionPrivateKey(): ECPrivateKey
    }
}