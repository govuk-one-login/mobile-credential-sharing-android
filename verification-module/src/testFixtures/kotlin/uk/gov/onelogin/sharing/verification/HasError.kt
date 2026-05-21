package uk.gov.onelogin.sharing.verification

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

internal class HasError(
    private val matcher: Matcher<in VerificationError>
) : TypeSafeMatcher<VerificationResult.Failure>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: VerificationResult.Failure?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(item?.error, mismatchDescription)

    override fun matchesSafely(item: VerificationResult.Failure?): Boolean =
        matcher.matches(item?.error)
}
