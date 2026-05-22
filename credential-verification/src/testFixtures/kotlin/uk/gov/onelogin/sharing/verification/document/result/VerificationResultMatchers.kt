package uk.gov.onelogin.sharing.verification.document.result

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object VerificationResultMatchers {
    fun hasError(expected: VerificationError): Matcher<in VerificationResult> =
        hasError(equalTo(expected))

    fun hasError(matcher: Matcher<in VerificationError>): Matcher<in VerificationResult> =
        VerificationResultMatcher(matcher) {
            (it as? VerificationResult.Failure)?.error
        }

    private class VerificationResultMatcher<Type>(
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
}
