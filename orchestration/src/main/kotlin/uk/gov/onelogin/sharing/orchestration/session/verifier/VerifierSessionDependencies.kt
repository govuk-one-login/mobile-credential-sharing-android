package uk.gov.onelogin.sharing.orchestration.session.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import uk.gov.logging.api.Logger

/**
 * Configuration interface for providing an instance of [VerifierSession] for use in dependency
 * injection via `metro`.
 */
@ContributesTo(AppScope::class)
interface VerifierSessionDependencies {
    @Provides
    fun providesVerifierSession(logger: Logger): VerifierSession = VerifierSessionImpl(
        logger = logger
    )
}
