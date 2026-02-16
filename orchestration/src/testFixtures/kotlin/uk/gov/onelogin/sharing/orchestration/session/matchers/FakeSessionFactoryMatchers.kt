package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState

/**
 * Collection of hamcrest [Matcher] functions for use with the [FakeSessionFactory].
 */
object FakeSessionFactoryMatchers {
    fun <Session : Any> currentSession(expected: Session): Matcher<in FakeSessionFactory<Session>> =
        currentSession(equalTo(expected))

    fun <Session : Any> currentSession(
        matcher: Matcher<in Session>
    ): Matcher<in FakeSessionFactory<Session>> = HasCurrentSession(matcher)

    fun <State : Any, Session : StateContainer<in State>> currentSessionState(
        expected: State
    ): Matcher<in FakeSessionFactory<Session>> = currentSession(hasCurrentState(expected))

    fun <State : Any, Session : StateContainer<in State>> currentSessionState(
        matcher: Matcher<in State>
    ): Matcher<in FakeSessionFactory<Session>> = currentSession(hasCurrentState(matcher))
}
