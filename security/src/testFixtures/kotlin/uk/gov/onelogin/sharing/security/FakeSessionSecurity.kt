package uk.gov.onelogin.sharing.security

import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

class FakeSessionSecurity : SessionSecurity {

    private lateinit var sessionKeyPair: KeyPair

    // Returns the public key for engagement
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair {
        sessionKeyPair = SessionSecurityTestStub.generateValidKeyPair()!!
        return sessionKeyPair
    }

    override fun generateSharedSecret(
        holderKey: ECPrivateKey,
        eReaderKey: ECPublicKey,
        logger: Logger
    ): ByteArray = byteArrayOf()

    override fun generateSessionPublicKey(): CoseKey {
        val keyPair = SessionSecurityTestStub.generateValidKeyPair()
        return CoseKey.generateCoseKey(keyPair?.public as ECPublicKey)
    }

    override fun getSessionPrivateKey(): ECPrivateKey = sessionKeyPair.private as ECPrivateKey

    override fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: String
    ): ByteArray = byteArrayOf()
}
