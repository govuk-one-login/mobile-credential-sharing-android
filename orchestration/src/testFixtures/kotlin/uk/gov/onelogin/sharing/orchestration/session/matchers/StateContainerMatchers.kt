package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.StateContainer

/**
 * Wrapper object for storing hamcrest [Matcher] functions for the [StateContainer] interface.
 */
object StateContainerMatchers {
    fun <State : Any> hasCurrentState(expected: State): Matcher<StateContainer<in State>> =
        hasCurrentState(equalTo(expected))

    fun <State : Any> hasCurrentState(
        matcher: Matcher<in State>
    ): Matcher<StateContainer<in State>> = HasCurrentState(matcher)
}
