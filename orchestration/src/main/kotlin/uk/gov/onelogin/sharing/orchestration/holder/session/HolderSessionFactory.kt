package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory

/**
 * [SessionFactory] implementation that provides new instances of [HolderSession].
 *
 * @see uk.gov.onelogin.orchestration.HolderOrchestrator
 */
@ContributesBinding(scope = AppScope::class)
class HolderSessionFactory(private val logger: Logger) : SessionFactory<HolderSession> {
    override fun create(): HolderSession = HolderSessionImpl(logger = logger)
}
