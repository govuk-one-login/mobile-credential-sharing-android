package uk.gov.onelogin.sharing.security.secureArea

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.secureArea.keypair.EcKeyPairGenerator
import uk.gov.onelogin.sharing.security.secureArea.keypair.MemorisedKeyGenerator

/**
 * Metro dependency injection configuration for the [KeyGenerator.KeyPairGenerator].
 */
@ContributesTo(ViewModelScope::class)
interface KeyGeneratorDependencies {

    /**
     * @return An instance of [MemorisedKeyGenerator] for storing the return value of
     * [EcKeyPairGenerator.generateEcKeyPair] in memory.
     */
    @Provides
    fun providesKeyPairGenerator(logger: Logger): KeyGenerator.KeyPairGenerator =
        MemorisedKeyGenerator(
            generator = EcKeyPairGenerator(logger),
            logger = logger
        )
}
