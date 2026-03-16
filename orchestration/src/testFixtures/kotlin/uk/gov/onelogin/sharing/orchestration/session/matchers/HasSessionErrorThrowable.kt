package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError

internal class HasSessionErrorThrowable(private val matcher: Matcher<in Throwable>) :
    TypeSafeMatcher<SessionError>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(item: SessionError?, mismatchDescription: Description?) =
        matcher.describeMismatch(
            item?.exception,
            mismatchDescription
        )

    override fun matchesSafely(item: SessionError?): Boolean = matcher.matches(
        item?.exception
    )
}
