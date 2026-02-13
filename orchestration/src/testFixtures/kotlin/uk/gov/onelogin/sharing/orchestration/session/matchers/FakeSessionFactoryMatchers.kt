package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory

object FakeSessionFactoryMatchers {
    fun <Session : Any> hasCurrentSession(
        expected: Session
    ): Matcher<FakeSessionFactory<Session>> = hasCurrentSession(equalTo(expected))

    fun <Session : Any> hasCurrentSession(
        matcher: Matcher<in Session>
    ): Matcher<FakeSessionFactory<Session>> = HasCurrentSession(matcher)
}

