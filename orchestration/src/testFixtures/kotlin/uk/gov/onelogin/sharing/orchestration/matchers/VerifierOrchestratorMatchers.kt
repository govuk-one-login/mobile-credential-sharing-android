package uk.gov.onelogin.sharing.orchestration.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.orchestration.VerifierOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSession

object VerifierOrchestratorMatchers {
    fun hasSession(
        matcher: Matcher<in VerifierSession>
    ): Matcher<VerifierOrchestrator> = HasVerifierSession(matcher)
}

