package uk.gov.onelogin.sharing.cryptoService

import java.security.KeyPair
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.cryptoService.secureArea.SessionSecurity
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class FakeSessionSecurity : SessionSecurity {
    lateinit var plaintextToReturn: ByteArray

    var lastDecryptData: ByteArray? = null
    var lastDecryptRole: DeviceRole? = null
    var lastDecryptCounter: UInt? = null

    private lateinit var sessionKeyPair: KeyPair

    // Returns the keypair for engagement
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair {
        sessionKeyPair = SessionSecurityTestStub.generateValidKeyPair()!!
        return sessionKeyPair
    }

    override fun generateSharedSecret(
        thisDevicePrivateKey: ECPrivateKey,
        otherDevicePublicKey: ECPublicKey
    ): ByteArray = byteArrayOf(1)

    override fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: DeviceRole
    ): ByteArray = byteArrayOf(2)

    override fun decryptPayload(
        key: ByteArray,
        data: ByteArray,
        role: DeviceRole,
        decryptCounter: UInt
    ): ByteArray {
        lastDecryptData = data
        lastDecryptRole = role
        lastDecryptCounter = decryptCounter
        return plaintextToReturn
    }
}
