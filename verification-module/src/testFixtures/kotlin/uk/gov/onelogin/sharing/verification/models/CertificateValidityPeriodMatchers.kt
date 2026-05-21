package uk.gov.onelogin.sharing.verification.models

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

@OptIn(ExperimentalTime::class)
object CertificateValidityPeriodMatchers {
    fun hasNotAfter(
        expected: Instant
    ) = hasNotAfter(equalTo(expected))
    fun hasNotAfter(
        matcher: Matcher<in Instant>
    ): Matcher<in CertificateValidityPeriod> = CertificateValidityPeriodMatcher(matcher) {
        it?.notAfter
    }
    fun hasNotBefore(
        expected: Instant
    ) = hasNotBefore(equalTo(expected))
    fun hasNotBefore(
        matcher: Matcher<in Instant>
    ): Matcher<in CertificateValidityPeriod> = CertificateValidityPeriodMatcher(matcher) {
        it?.notBefore
    }

    private class CertificateValidityPeriodMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (CertificateValidityPeriod?) -> Type?
    ) : TypeSafeMatcher<CertificateValidityPeriod>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: CertificateValidityPeriod?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)
        override fun matchesSafely(item: CertificateValidityPeriod?): Boolean =
            matcher.matches(transformer(item))
    }

}

