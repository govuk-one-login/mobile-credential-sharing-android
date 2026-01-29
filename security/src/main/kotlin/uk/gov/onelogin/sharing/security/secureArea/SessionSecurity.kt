package uk.gov.onelogin.sharing.security.secureArea

import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator

/**
 * Wrapper interface for holding cryptographic operations throughout a User's session.
 */
interface SessionSecurity :
    KeyGenerator.KeyPairGenerator,
    KeyGenerator.PrivateKeyGenerator,
    KeyGenerator.PublicKeyGenerator,
    SharedSecretGenerator {

    fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: DeviceRole
    ): ByteArray

    companion object {
        enum class DeviceRole {
            VERIFIER,
            HOLDER
        }
    }
}
