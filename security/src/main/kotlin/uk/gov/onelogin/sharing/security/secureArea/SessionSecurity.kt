package uk.gov.onelogin.sharing.security.secureArea

import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.cose.CoseKey

interface SessionSecurity {
    fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair?

    fun generateSharedSecret(
        holderKey: ECPrivateKey,
        eReaderKey: ECPublicKey,
        logger: Logger
    ): ByteArray

    fun generateSessionPublicKey(): CoseKey

    fun getSessionPrivateKey(): ECPrivateKey

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
