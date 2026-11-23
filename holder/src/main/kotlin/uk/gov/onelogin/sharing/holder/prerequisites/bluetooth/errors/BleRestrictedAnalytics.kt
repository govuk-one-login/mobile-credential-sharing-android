package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent

/**
 * Analytics identities for the BLE Permissions Restricted error screen.
 */
internal object BleRestrictedAnalytics {
    internal const val SCREEN_ID = "19401e1a-55fb-4672-93f7-9e36eb62e22b"

    internal fun AnalyticsLogger.bleRestrictedScreen(name: String) = screenEvent(
        title = name,
        id = SCREEN_ID
    )

    internal fun AnalyticsLogger.bleRestrictedGoBackButton(text: String) = buttonEvent(
        text = text
    )
}
