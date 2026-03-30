package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

internal class IsUnrecoverableThrowable(
    private val matcher: Matcher<in SessionErrorReason.UnrecoverableThrowable>
) : TypeSafeMatcher<SessionErrorReason>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: SessionErrorReason?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item, mismatchDescription)

    override fun matchesSafely(item: SessionErrorReason?): Boolean = matcher.matches(item)
}
