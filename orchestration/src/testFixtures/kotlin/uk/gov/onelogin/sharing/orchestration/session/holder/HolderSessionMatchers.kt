package uk.gov.onelogin.sharing.orchestration.session.holder

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

/**
 * Wrapper object for storing hamcrest [Matcher] functions for the [HolderSession] interface.
 */
object HolderSessionMatchers {
    fun hasCurrentState(
        expected: HolderSessionState
    ): Matcher<HolderSession> = hasCurrentState(CoreMatchers.equalTo(expected))

    fun hasCurrentState(
        matcher: Matcher<HolderSessionState>
    ): Matcher<HolderSession> = HasCurrentState(matcher)
}