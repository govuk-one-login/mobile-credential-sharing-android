package uk.gov.onelogin.sharing.holder.consent

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent

internal object HolderConsentAnalyticsEvents {

    private const val SCREEN_ID = "ac1f2e03-2b4e-4804-8c16-2d18c37b0f9e"

    internal fun AnalyticsLogger.reviewWhatYouWillShareScreen(screenName: String) = screenEvent(
        title = screenName,
        id = SCREEN_ID
    )

    internal fun AnalyticsLogger.readPrivacyNoticeButton() = buttonEvent(
        text = "Read about DVS and their privacy notice"
    )

    internal fun AnalyticsLogger.agreeAndShareButton() = buttonEvent(
        text = "Agree and share details"
    )

    internal fun AnalyticsLogger.cancelAndGoBackButton() = buttonEvent(
        text = "Cancel and go back to documents"
    )

    internal fun AnalyticsLogger.moreAboutDvsScreen(name: String) = screenEvent(
        title = name,
        id = "288470f5-8bdb-43cf-8cd7-38bcd241413b"
    )
}
