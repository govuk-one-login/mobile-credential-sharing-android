package uk.gov.onelogin.sharing.security

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity.Companion.DeviceRole
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

object SessionSecurityTestStub {
    const val ALGORITHM = "EC"
    const val PARAMETER_SPEC = "secp256r1"
    const val UNSUPPORTED_PARAMETER_SPEC = "secp384r1"

    val sessionSecurity = SessionSecurityImpl(SystemLogger())

    fun generateValidPublicKeyPair(): ECPublicKey {
        val publicKey = sessionSecurity.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        return publicKey?.public as ECPublicKey
    }

    fun getKeyParameter(): ECParameterSpec {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(
            ECGenParameterSpec(PARAMETER_SPEC)
        )
        return (keyPairGenerator.generateKeyPair().public as ECPublicKey).params
    }

    fun generateValidKeyPair(): KeyPair? {
        val keyPair = sessionSecurity.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        return keyPair
    }

    fun generateValidUnsupportedKeyPair(): KeyPair? {
        val keyPair = sessionSecurity.generateEcKeyPair(ALGORITHM, UNSUPPORTED_PARAMETER_SPEC)
        return keyPair
    }

    fun getSharedSecret(holderPrivateKey: ECPrivateKey, readerPublicKey: ECPublicKey): ByteArray =
        sessionSecurity.generateSharedSecret(
            holderPrivateKey,
            readerPublicKey,
            SystemLogger()
        )

    fun generateSessionKey(role: DeviceRole): ByteArray = when (role) {
        DeviceRole.VERIFIER -> "58d277d8719e62a1561d248f403f477e9e6c37bf5d5fc5126f8f4c727c22dfc9"
        DeviceRole.HOLDER -> "81d170e07fbdac93c1a676242c2576124a380d87bb73ed9ce4834de2272cf409"
    }.hexToByteArray()
}
