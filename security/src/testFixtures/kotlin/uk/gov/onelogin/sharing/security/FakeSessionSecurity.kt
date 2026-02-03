package uk.gov.onelogin.sharing.security

import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class FakeSessionSecurity : SessionSecurity {

    private lateinit var sessionKeyPair: KeyPair

    // Returns the public key for engagement
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair {
        sessionKeyPair = SessionSecurityTestStub.generateValidKeyPair()!!
        return sessionKeyPair
    }

    override fun generateSharedSecret(holderKey: ECPrivateKey, eReaderKey: ECPublicKey): ByteArray =
        byteArrayOf()

    override fun generateSessionPublicKey(): CoseKey {
        val keyPair = SessionSecurityTestStub.generateValidKeyPair()
        return CoseKey.generateCoseKey(keyPair?.public as ECPublicKey)
    }

    override fun getSessionPrivateKey(): ECPrivateKey = sessionKeyPair.private as ECPrivateKey

    override fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: DeviceRole
    ): ByteArray = byteArrayOf()

    override fun decryptPayload(key: ByteArray, data: ByteArray, role: DeviceRole): ByteArray =
        byteArrayOf()
}
