package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError

object SessionErrorMatchers {
    fun hasThrowable(matcher: Matcher<in Throwable>): Matcher<SessionError> =
        HasSessionErrorThrowable(matcher)

    fun hasMessage(matcher: Matcher<in String>): Matcher<SessionError> =
        HasSessionErrorMessage(matcher)
}
