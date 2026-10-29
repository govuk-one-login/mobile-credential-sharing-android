package uk.gov.onelogin.sharing.holder.gettingdetails

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Self-contained GA4 analytics for the "Getting list of details" loading screen
 * (shown during [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingEstablishment]).
 *
 * Builds and dispatches the analytics events directly via the supplied [AnalyticsLogger]:
 *  - a screenView event for [FIREBASE_SCREEN_ID]
 *  - a trackEventButton event when the Cancel/Close control is pressed
 *
 * Taxonomy (taxonomy_level_1 = "one login mobile application", taxonomy_level_2 = "wallet",
 * taxonomy_level_3 = "proximity sharing") is supplied by [walletSharingRequiredParameters].
 */
object GettingDetailsAnalytics {
    private const val SCREEN_NAME = "Getting list of details"

    // TODO: Confirm the firebaseScreenID for the ProcessingEstablishment screen.
    // Placeholder until provided by product/analytics.
    private const val FIREBASE_SCREEN_ID = "TODO-PROCESSING-ESTABLISHMENT-SCREEN-ID"
    private const val CLOSE_BUTTON_TEXT = "Close"

    fun logScreenView(analyticsLogger: AnalyticsLogger) =
        ViewEvent.Screen(
            name = SCREEN_NAME,
            id = FIREBASE_SCREEN_ID,
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)

    fun logCloseButton(analyticsLogger: AnalyticsLogger) =
        TrackEvent.Button(
            text = CLOSE_BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).let(analyticsLogger::logEventV3Dot1)
}
