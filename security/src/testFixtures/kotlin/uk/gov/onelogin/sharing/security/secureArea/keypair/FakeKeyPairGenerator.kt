package uk.gov.onelogin.sharing.security.secureArea.keypair

import java.security.KeyPair
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

class FakeKeyPairGenerator(private val keyPairs: List<KeyPair?>) : KeyGenerator.KeyPairGenerator {

    constructor(vararg keyPairs: KeyPair?) : this(
        keyPairs = keyPairs.asList()
    )

    var interactionCount: Int = 0
        private set

    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair? {
        val result = keyPairs[interactionCount]
        interactionCount += 1
        return result
    }
}
