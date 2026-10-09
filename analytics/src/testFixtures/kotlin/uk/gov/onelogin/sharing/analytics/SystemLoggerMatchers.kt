package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.AnalyticsEvent
import uk.gov.logging.testdouble.v2.LogEntry
import uk.gov.onelogin.sharing.analytics.AnalyticsEventTestData.sampleLegacyScreenEvent

object SystemLoggerMatchers {
    fun containsEventState(
        analyticsEvent: AnalyticsEvent = sampleLegacyScreenEvent
    ): (LogEntry) -> Boolean = { entry ->
        entry.message.contains("Received analytics event: $analyticsEvent")
    }
    val containsUnloggableEvent: (LogEntry) -> Boolean = { entry ->
        entry.message.contains(
            "Received an event that shouldn't be logged!"
        )
    }
}
