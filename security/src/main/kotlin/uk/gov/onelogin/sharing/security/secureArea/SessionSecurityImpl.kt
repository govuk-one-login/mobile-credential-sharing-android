package uk.gov.onelogin.sharing.security.secureArea

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cryptography.java.generateSalt
import uk.gov.onelogin.sharing.security.cryptography.java.hkdfKeyGeneration
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity.Companion.DeviceRole
import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session via .
 *
 * Uses interface delegation to provide the necessary features.
 */
@ContributesBinding(ViewModelScope::class, binding = binding<SessionSecurity>())
class SessionSecurityImpl(
    keyPairGenerator: KeyGenerator.KeyPairGenerator,
    privateKeyGenerator: KeyGenerator.PrivateKeyGenerator,
    publicKeyGenerator: KeyGenerator.PublicKeyGenerator,
    secretGenerator: SharedSecretGenerator
) : SessionSecurity,
    KeyGenerator.Complete,
    KeyGenerator.KeyPairGenerator by keyPairGenerator,
    KeyGenerator.PrivateKeyGenerator by privateKeyGenerator,
    KeyGenerator.PublicKeyGenerator by publicKeyGenerator,
    SharedSecretGenerator by secretGenerator {

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
        val roleAsBytes = when (role) {
            DeviceRole.VERIFIER -> "SKReader"
            DeviceRole.HOLDER -> "SKDevice"
        }.encodeCbor()

        return hkdfKeyGeneration(
            sharedKey,
            salt,
            roleAsBytes
        )
    }
}
