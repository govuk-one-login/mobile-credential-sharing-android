package uk.gov.onelogin.sharing.cryptoService.secureArea

import java.security.KeyPair

fun interface KeyPairGenerator {
    /**
     * Generates a new, ephemeral key pair.
     *
     * The public key is intended to be shared with the verifier application as part of the
     * device engagement process.
     *
     * @return A [java.security.KeyPair] object used when communicating between devices.
     */
    fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair?
}
