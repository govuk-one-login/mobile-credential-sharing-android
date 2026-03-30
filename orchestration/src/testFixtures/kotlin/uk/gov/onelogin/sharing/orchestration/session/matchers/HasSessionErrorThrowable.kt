package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

internal class HasSessionErrorThrowable(private val matcher: Matcher<in Throwable>) :
    TypeSafeMatcher<SessionErrorReason.UnrecoverableThrowable>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: SessionErrorReason.UnrecoverableThrowable?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        item?.exception,
        mismatchDescription
    )

    override fun matchesSafely(item: SessionErrorReason.UnrecoverableThrowable?): Boolean =
        matcher.matches(item?.exception)
}
