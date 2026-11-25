package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent

internal object DeviceBleDeniedAnalytics {
    internal const val SCREEN_ID = "64d8556e-c6ec-4d90-812d-2ddfc834d677"

    internal fun AnalyticsLogger.deviceBleDeniedScreen(screenName: String) =
        screenEvent(title = screenName, id = SCREEN_ID)
}
