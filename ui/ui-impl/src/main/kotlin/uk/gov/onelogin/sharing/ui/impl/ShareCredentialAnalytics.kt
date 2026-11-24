package uk.gov.onelogin.sharing.ui.impl

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent

/**
 * Analytics identities for the [ShareCredential] screen.
 *
 * Co-located with the screen it represents so that button identity is owned by this module
 * rather than the analytics module.
 */
internal object ShareCredentialAnalytics {
    internal fun AnalyticsLogger.closeButton() = buttonEvent(
        text = "Close"
    )
}
