package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Analytics identities for the BLE Permissions Restricted error screen.
 *
 * Co-located with the screen it represents so that screen/button identity (names and IDs)
 * is owned by this feature module. The English screen/button text is resolved by the UI and
 * passed in, keeping [android.content.Context] out of this layer and the ViewModel.
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
