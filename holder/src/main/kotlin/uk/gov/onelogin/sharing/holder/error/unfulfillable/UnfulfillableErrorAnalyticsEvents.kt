package uk.gov.onelogin.sharing.holder.error.unfulfillable

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.errorScreenEvent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.SuccessReason

internal object UnfulfillableErrorAnalyticsEvents {
    internal fun AnalyticsLogger.unfulfillableErrorScreen(
        name: String,
        reason: SuccessReason
    ) = errorScreenEvent(
    name = name,
    id = "1c5c61b3-b8d4-4355-9be3-b26440a43b3f",
    reason = reason.toString(),
    )
}