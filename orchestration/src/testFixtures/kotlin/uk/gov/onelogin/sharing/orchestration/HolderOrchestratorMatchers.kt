package uk.gov.onelogin.sharing.orchestration

import org.hamcrest.Matcher
import uk.gov.onelogin.orchestration.HolderOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession

/**
 * Wrapper object for storing hamcrest [Matcher] functions for the [HolderOrchestrator].
 */
object HolderOrchestratorMatchers {
    /**
     * @see HolderOrchestratorHasSession
     */
    fun hasSession(matcher: Matcher<HolderSession>): Matcher<HolderOrchestrator> =
        HolderOrchestratorHasSession(matcher)
}
