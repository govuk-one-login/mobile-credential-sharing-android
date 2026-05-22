package uk.gov.onelogin.sharing.verification.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

@OptIn(ExperimentalTime::class)
object ValidityInfoMatchers {
    fun hasExpectedUpdate(expected: Instant): Matcher<in ValidityInfo> =
        hasExpectedUpdate(equalTo(expected))

    fun hasExpectedUpdate(matcher: Matcher<in Instant>): Matcher<in ValidityInfo> =
        ValidityInfoMatcher(matcher) { it?.expectedUpdate }

    fun hasSigned(expected: Instant) = hasSigned(equalTo(expected))

    fun hasSigned(matcher: Matcher<in Instant>): Matcher<in ValidityInfo> =
        ValidityInfoMatcher(matcher) { it?.signed }

    fun hasValidFrom(expected: Instant) = hasValidFrom(equalTo(expected))

    fun hasValidFrom(matcher: Matcher<in Instant>): Matcher<in ValidityInfo> =
        ValidityInfoMatcher(matcher) { it?.validFrom }

    fun hasValidUntil(expected: Instant) = hasValidUntil(equalTo(expected))

    fun hasValidUntil(matcher: Matcher<in Instant>): Matcher<in ValidityInfo> =
        ValidityInfoMatcher(matcher) { it?.validUntil }

    private class ValidityInfoMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (ValidityInfo?) -> Type?
    ) : TypeSafeMatcher<ValidityInfo>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: ValidityInfo?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: ValidityInfo?): Boolean =
            matcher.matches(transformer(item))
    }
}
