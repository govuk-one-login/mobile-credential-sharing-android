package uk.gov.onelogin.sharing.verification.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

@OptIn(ExperimentalTime::class)
object ValidityInfoMatchers {
    fun hasExpectedUpdate(
        expected: Instant
    ): Matcher<in ValidityInfo> = hasExpectedUpdate(equalTo(expected))

    fun hasExpectedUpdate(
        matcher: Matcher<in Instant>
    ): Matcher<in ValidityInfo> = ValidityInfoMatcher(matcher) { it?.expectedUpdate }

    fun hasSigned(
        expected: Instant
    ) = hasSigned(equalTo(expected))

    fun hasSigned(
        matcher: Matcher<in Instant>
    ): Matcher<in ValidityInfo> = ValidityInfoMatcher(matcher) { it?.signed }

    fun hasValidFrom(
        expected: Instant
    ) = hasValidFrom(equalTo(expected))

    fun hasValidFrom(
        matcher: Matcher<in Instant>
    ): Matcher<in ValidityInfo> = ValidityInfoMatcher(matcher) { it?.validFrom }

    fun hasValidUntil(
        expected: Instant
    ) = hasValidUntil(equalTo(expected))

    fun hasValidUntil(
        matcher: Matcher<in Instant>
    ): Matcher<in ValidityInfo> = ValidityInfoMatcher(matcher) { it?.validUntil }
}
