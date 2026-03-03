package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class IsMissingPermission(private val matcher: Matcher<in Iterable<String>>) :
    TypeSafeMatcher<UnauthorizedReason>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: UnauthorizedReason?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? UnauthorizedReason.MissingPermissions)?.missingPermissions,
        mismatchDescription
    )

    override fun matchesSafely(item: UnauthorizedReason?): Boolean = matcher.matches(
        (item as? UnauthorizedReason.MissingPermissions)?.missingPermissions
    )
}
