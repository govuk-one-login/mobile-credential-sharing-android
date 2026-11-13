package uk.gov.onelogin.sharing.holder.error.generic

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent

internal object GenericHolderErrorAnalyticsEvents {
    internal fun AnalyticsLogger.genericHolderErrorScreen(name: String, reason: String) =
        errorScreenEvent(
            name = name,
            id = "9bf821af-e8ff-41cf-a311-4b6cec989315",
            reason = reason
        )
}
