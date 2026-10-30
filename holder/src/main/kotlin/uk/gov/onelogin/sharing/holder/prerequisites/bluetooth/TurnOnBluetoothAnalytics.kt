package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Analytics identities for the Turn on Bluetooth screen.
 *
 * Co-located with the screen it represents so that screen/button identity (names and IDs)
 * is owned by this feature module
 */
internal object TurnOnBluetoothAnalytics {

    internal fun AnalyticsLogger.turnOnBluetoothScreen() = Screen(
        name = "Turn on Bluetooth",
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.turnOnBluetoothButton() = Button(
        text = "Turn on Bluetooth",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
