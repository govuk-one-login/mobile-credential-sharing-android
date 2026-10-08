package uk.gov.onelogin.sharing.analytics

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains
import org.hamcrest.TypeSafeMatcher
import uk.gov.logging.api.analytics.AnalyticsEvent

object StoredAnalyticsLoggerMatchers {
    fun hasSentEvents(
        matcher: Matcher<in Iterable<AnalyticsEvent>>
    ): Matcher<in StoredAnalyticsLogger> = StoredAnalyticsLoggerMatcher(matcher) {
        it?.sentAnalyticsEvents
    }

    fun hasIgnoredEvents(
        matcher: Matcher<in Iterable<AnalyticsEvent>>
    ): Matcher<in StoredAnalyticsLogger> = StoredAnalyticsLoggerMatcher(matcher) {
        it?.ignoredAnalyticsEvents
    }

    private class StoredAnalyticsLoggerMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (StoredAnalyticsLogger?) -> Type?
    ) : TypeSafeMatcher<StoredAnalyticsLogger>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)

        override fun describeMismatchSafely(
            item: StoredAnalyticsLogger?,
            mismatchDescription: Description?
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: StoredAnalyticsLogger?): Boolean =
            matcher.matches(transformer(item))
    }
}
