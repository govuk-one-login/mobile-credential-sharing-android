package uk.gov.onelogin.sharing.orchestration.verifier.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory

@ContributesBinding(scope = AppScope::class)
class VerifierSessionFactory(private val logger: Logger) : SessionFactory<VerifierSession> {
    override fun create(): VerifierSession = VerifierSessionImpl(logger = logger)
}
