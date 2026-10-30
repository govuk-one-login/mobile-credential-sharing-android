package uk.gov.onelogin.sharing.holder.consent

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

internal object HolderConsentAnalyticsEvents {

    private const val SCREEN_ID = "ac1f2e03-2b4e-4804-8c16-2d18c37b0f9e"

    internal fun AnalyticsLogger.consentScreen() = Screen(
        name = "Review what you'll share",
        id = SCREEN_ID,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.readPrivacyNoticeButton() = Button(
        text = "Read about and their privacy notice",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.agreeAndShareButton() = Button(
        text = "Agree and share details",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.cancelAndGoBackButton() = Button(
        text = "Cancel and go back to documents",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
