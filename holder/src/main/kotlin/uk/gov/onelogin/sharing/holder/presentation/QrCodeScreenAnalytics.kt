package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Analytics identity for the QR code screen.
 *
 * The screen name and id are owned and declared by this feature module and consumed by
 * [QrCodeViewModel] when sending the screen view event.
 */
internal object QrCodeScreenAnalytics {
    internal fun AnalyticsLogger.qrCodeScreen(name: String) = Screen(
        name = name,
        id = "29fd14a3-4390-4939-9d57-d9fe8cf61811",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
