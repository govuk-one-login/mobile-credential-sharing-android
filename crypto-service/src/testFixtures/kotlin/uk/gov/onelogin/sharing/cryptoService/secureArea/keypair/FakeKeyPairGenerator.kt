package uk.gov.onelogin.sharing.cryptoService.secureArea.keypair

import java.security.KeyPair
import uk.gov.onelogin.sharing.cryptoService.secureArea.KeyPairGenerator

class FakeKeyPairGenerator(private val keyPairs: List<KeyPair?>) : KeyPairGenerator {

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
