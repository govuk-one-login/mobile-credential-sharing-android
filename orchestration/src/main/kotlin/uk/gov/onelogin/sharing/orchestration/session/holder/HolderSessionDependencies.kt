package uk.gov.onelogin.sharing.orchestration.session.holder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import uk.gov.logging.api.Logger

/**
 * Configuration interface for providing an instance of [HolderSession] for use in dependency
 * injection via `metro`.
 */
@ContributesTo(AppScope::class)
interface HolderSessionDependencies {
    @Provides
    fun providesHolderSession(logger: Logger): HolderSession = HolderSessionImpl(
        logger = logger
    )
}
