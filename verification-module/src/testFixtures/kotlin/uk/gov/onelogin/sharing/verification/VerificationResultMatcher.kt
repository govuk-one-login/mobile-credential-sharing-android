package uk.gov.onelogin.sharing.verification

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.verification.result.VerificationResult

internal class VerificationResultMatcher<Type>(
    private val matcher: Matcher<in Type>,
    private val transformer: (VerificationResult?) -> Type?
) : TypeSafeMatcher<VerificationResult>() {
    override fun describeTo(description: Description?) = matcher.describeTo(description)
    override fun describeMismatchSafely(
        item: VerificationResult?,
        mismatchDescription: Description?
    ) = matcher.describeMismatch(transformer(item), mismatchDescription)

    override fun matchesSafely(item: VerificationResult?): Boolean =
        matcher.matches(transformer(item))
}
