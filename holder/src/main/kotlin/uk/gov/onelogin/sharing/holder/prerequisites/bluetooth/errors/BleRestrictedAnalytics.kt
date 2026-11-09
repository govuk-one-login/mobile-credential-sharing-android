package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Analytics identities for the BLE Permissions Restricted error screen.
 */
internal object BleRestrictedAnalytics {
    internal const val SCREEN_ID = "19401e1a-55fb-4672-93f7-9e36eb62e22b"

    internal fun AnalyticsLogger.bleRestrictedScreen(name: String) = Screen(
        name = name,
        id = SCREEN_ID,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.bleRestrictedGoBackButton(text: String) = Button(
        text = text,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
