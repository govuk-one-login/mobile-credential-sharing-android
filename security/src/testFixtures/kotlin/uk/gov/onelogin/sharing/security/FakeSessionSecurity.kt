package uk.gov.onelogin.sharing.security

import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

class FakeSessionSecurity(private val keyPair: KeyPair?) : SessionSecurity {

    // Returns the public key for engagement
    override fun generateEcKeyPair(
        algorithm: String,
        parameterSpec: String
    ): KeyPair? {
        return keyPair
    }
    override fun generateSharedSecret(
        holderKey: ECPrivateKey,
        eReaderKey: ECPublicKey,
        logger: Logger
    ): ByteArray = byteArrayOf()
}
