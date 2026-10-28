package uk.gov.onelogin.sharing.analytics

import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains
import uk.gov.logging.api.analytics.AnalyticsEvent
import uk.gov.logging.api.v3.LogEntry
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.logging.api.v3.matchers.LogEntryMatchers.hasMessage
import uk.gov.onelogin.sharing.analytics.AnalyticsEventTestData.sampleLegacyScreenEvent

object SystemLoggerMatchers {
    fun containsEventState(
        analyticsEvent: AnalyticsEvent = sampleLegacyScreenEvent
    ): Matcher<in MemorisedLogger> = contains(
        hasMessage("Received analytics event: $analyticsEvent")
    )

    fun containsUnloggableEvent(): Matcher<in MemorisedLogger> = contains(
        hasMessage("Received an event that shouldn't be logged!")
    )
}
