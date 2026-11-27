package uk.gov.onelogin.sharing.holder.error.readerauth

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent

internal object ReaderAuthErrorAnalyticsEvents {
    internal fun AnalyticsLogger.readerAuthErrorScreen(name: String, reason: String) =
        errorScreenEvent(
            name = name,
            id = "d3e44133-024c-4ce8-84bf-7bed9c01ff96",
            reason = reason
        )
}
