package uk.gov.onelogin.sharing.holder.loading

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object LoadingAnalytics {
    private const val CLOSE_BUTTON_TEXT = "Close"

    fun logScreenView(analyticsLogger: AnalyticsLogger, type: LoadingScreenType) = ViewEvent.Screen(
        name = type.screenName,
        id = type.firebaseScreenId,
        params = walletSharingRequiredParameters
    ).let(analyticsLogger::logEventV3Dot1)

    fun logCloseButton(analyticsLogger: AnalyticsLogger) = TrackEvent.Button(
        text = CLOSE_BUTTON_TEXT,
        params = walletSharingRequiredParameters
    ).let(analyticsLogger::logEventV3Dot1)
}
