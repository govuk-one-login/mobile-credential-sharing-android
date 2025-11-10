package uk.gov.onelogin.sharing.security

import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurityImpl

object SessionSecurityTestStub {
    const val ALGORITHM = "EC"
    const val PARAMETER_SPEC = "secp256r1"
    val sessionSecurity = SessionSecurityImpl()

    fun generateValidKeyPair(): ECPublicKey? {
        val publicKey = sessionSecurity.generateEcPublicKey(ALGORITHM, PARAMETER_SPEC)
        return publicKey
    }

    fun generateInvalidKeyPair(): ECPublicKey? {
        val publicKey = sessionSecurity.generateEcPublicKey("INVALID_ALGO", "INVALID_SPEC")
        return publicKey
    }

    fun generateInvalidKeyPairWithValidAlgorithm(): ECPublicKey? {
        val publicKey = sessionSecurity.generateEcPublicKey(ALGORITHM, "INVALID_SPEC")
        return publicKey
    }

    fun getKeyParameter(): ECParameterSpec {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(
            ECGenParameterSpec(PARAMETER_SPEC)
        )
        return (keyPairGenerator.generateKeyPair().public as ECPublicKey).params
    }
}
