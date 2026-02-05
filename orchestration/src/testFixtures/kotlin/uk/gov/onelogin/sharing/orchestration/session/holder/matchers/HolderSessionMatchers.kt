package uk.gov.onelogin.sharing.orchestration.session.holder.matchers

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSession
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState

/**
 * Wrapper object for storing hamcrest [Matcher] functions for the [HolderSession] interface.
 */
object HolderSessionMatchers {
    fun hasCurrentState(expected: HolderSessionState): Matcher<HolderSession> =
        hasCurrentState(CoreMatchers.equalTo(expected))

    fun hasCurrentState(matcher: Matcher<HolderSessionState>): Matcher<HolderSession> =
        HasCurrentState(matcher)

    fun inPreflight(): Matcher<HolderSession> = hasCurrentState(
        HolderSessionStateMatchers.inPreflight()
    )
}
