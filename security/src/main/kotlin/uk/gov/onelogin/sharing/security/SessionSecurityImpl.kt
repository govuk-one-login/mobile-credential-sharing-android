package uk.gov.onelogin.sharing.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec

class SessionSecurityImpl : SessionSecurity {
    public override val publicKey: PublicKey
    private val keyPair: KeyPair

    init {
        val spec = KeyGenParameterSpec.Builder(
            "eckeypair",
            KeyProperties.PURPOSE_AGREE_KEY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .build()

        val generator = KeyPairGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_EC)
        generator.initialize(spec)

        keyPair = generator.generateKeyPair()
        publicKey = keyPair.public
    }
}
