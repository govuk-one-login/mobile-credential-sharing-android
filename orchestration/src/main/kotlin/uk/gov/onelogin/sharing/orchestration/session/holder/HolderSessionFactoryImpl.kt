package uk.gov.onelogin.sharing.orchestration.session.holder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger

@ContributesBinding(scope = AppScope::class)
class HolderSessionFactoryImpl(
    private val logger: Logger,
) : HolderSessionFactory {
    override fun create(): HolderSession = HolderSessionImpl(logger = logger)
}
