package uk.gov.onelogin.sharing.security.secureArea.keypair

import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.KeyPair
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

/**
 * [KeyGenerator.KeyPairGenerator] decorator implementation that primarily defers to the provided
 * [generator] for creating [java.security.KeyPair] instances.
 *
 * Internally stores the last successful [java.security.KeyPair] via the [sessionKeyPair] property.
 */
@ContributesIntoSet(ViewModelScope::class, binding = binding<Resettable>())
class MemorisedKeyGenerator(
    private val generator: KeyGenerator.KeyPairGenerator,
    private val logger: Logger
) : KeyGenerator.KeyPairGenerator,
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
            "Generated new session KeyPair"
        } else {
            "Using stored session KeyPair"
        }.also {
            logger.debug(
                logTag,
                it
            )
        }

        return sessionKeyPair
    }

    override fun reset() {
        sessionKeyPair = null
        logger.debug(
            logTag,
            "Cleared session KeyPair"
        )
    }
}
