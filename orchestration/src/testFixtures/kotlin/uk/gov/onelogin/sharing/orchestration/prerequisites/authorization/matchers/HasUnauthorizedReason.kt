package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.matchers

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason

internal class HasUnauthorizedReason(private val matcher: Matcher<in UnauthorizedReason>) :
    TypeSafeMatcher<AuthorizationResponse>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)

    override fun describeMismatchSafely(
        item: AuthorizationResponse?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(
        (item as? AuthorizationResponse.Unauthorized)?.reason,
        mismatchDescription
    )

    override fun matchesSafely(item: AuthorizationResponse?): Boolean =
        matcher.matches((item as? AuthorizationResponse.Unauthorized)?.reason)
}
