package uk.gov.onelogin.sharing.orchestration.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.orchestration.HolderOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession

object HolderOrchestratorMatchers {
    fun hasSession(matcher: Matcher<in HolderSession>): Matcher<HolderOrchestrator> =
        HasHolderSession(matcher)
}
