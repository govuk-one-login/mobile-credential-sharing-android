package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

object SessionErrorMatchers {
    fun hasMessage(matcher: Matcher<in String>): Matcher<SessionError> =
        HasSessionErrorMessage(matcher)

    fun hasReason(matcher: Matcher<in SessionErrorReason>): Matcher<SessionError> =
        HasSessionErrorReason(matcher)
}
