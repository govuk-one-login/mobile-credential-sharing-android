package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

object SessionErrorReasonMatchers {

    fun isUnrecoverableThrowable(
        matcher: Matcher<in SessionErrorReason.UnrecoverableThrowable> = instanceOf(
            SessionErrorReason.UnrecoverableThrowable::class.java
        )
    ): Matcher<in SessionErrorReason> = IsUnrecoverableThrowable(matcher)

    fun isUnrecoverablePrerequisite(
        matcher: Matcher<in SessionErrorReason.UnrecoverablePrerequisite> = instanceOf(
            SessionErrorReason.UnrecoverablePrerequisite::class.java
        )
    ): Matcher<in SessionErrorReason> = IsUnrecoverablePrerequisite(matcher)

    object UnrecoverableThrowableMatchers {
        fun hasSessionErrorThrowable(
            matcher: Matcher<in Throwable>
        ): Matcher<in SessionErrorReason.UnrecoverableThrowable> = HasSessionErrorThrowable(matcher)
    }
}
