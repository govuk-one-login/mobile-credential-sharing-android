package uk.gov.onelogin.sharing.security

import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec

object SessionSecurityTestStub {
    const val ALGORITHM = "EC"
    const val PARAMETER_SPEC = "secp256r1"

    fun getKeyParameter(): ECParameterSpec {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM)
        keyPairGenerator.initialize(
            ECGenParameterSpec(PARAMETER_SPEC)
        )
        return (keyPairGenerator.generateKeyPair().public as ECPublicKey).params
    }
}
