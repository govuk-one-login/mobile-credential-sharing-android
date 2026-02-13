package uk.gov.onelogin.sharing.orchestration.session.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger

@ContributesBinding(scope = AppScope::class)
class VerifierSessionFactoryImpl(
    private val logger: Logger,
) : VerifierSessionFactory {
    override fun create(): VerifierSession = VerifierSessionImpl(logger = logger)
}
