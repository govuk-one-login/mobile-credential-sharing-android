package uk.gov.onelogin.sharing.models.mdoc.sessionData

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class HasStatus(private val matcher: Matcher<in UInt>) :
    TypeSafeMatcher<SessionDataDto>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(item: SessionDataDto?, mismatchDescription: Description?) =
        matcher.describeMismatch(item?.status, mismatchDescription)

    override fun matchesSafely(item: SessionDataDto?): Boolean = matcher.matches(item?.status)
}
