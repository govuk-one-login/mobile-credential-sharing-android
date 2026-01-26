package uk.gov.onelogin.sharing.security.secureArea.keypair

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.KeyPair
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

/**
 * [KeyGenerator.KeyPairGenerator] decorator implementation that primarily defers to the provided
 * [generator] for creating [java.security.KeyPair] instances.
 *
 * Internally stores the last successful [java.security.KeyPair] via the [sessionKeyPair] property.
 */
@ContributesIntoSet(ViewModelScope::class, binding = binding<Resettable>())
class MemorisedKeyGenerator(private val generator: KeyGenerator.KeyPairGenerator) :
    KeyGenerator.KeyPairGenerator,
    Resettable {
    /**
     * The last successfully generated [java.security.KeyPair].
     */
    private var sessionKeyPair: KeyPair? = null

    /**
     * @return [sessionKeyPair] when it's not null. Otherwise, the result of [generator]'s
     * [KeyGenerator.KeyPairGenerator.generateEcKeyPair] after storing it in memory.
     */
    override fun generateEcKeyPair(algorithm: String, parameterSpec: String): KeyPair? {
        if (sessionKeyPair == null) {
            sessionKeyPair = generator.generateEcKeyPair(algorithm, parameterSpec)
        }

        return sessionKeyPair
    }

    override fun reset() {
        sessionKeyPair = null
    }
}
