package uk.gov.onelogin.sharing.orchestration.session.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

internal class HasSessionErrorReason(private val matcher: Matcher<in SessionErrorReason>) :
    TypeSafeMatcher<SessionError>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(item: SessionError?, mismatchDescription: Description?) =
        matcher.describeMismatch(
            item?.reason,
            mismatchDescription
        )

    override fun matchesSafely(item: SessionError?): Boolean = matcher.matches(
        item?.reason
    )
}
