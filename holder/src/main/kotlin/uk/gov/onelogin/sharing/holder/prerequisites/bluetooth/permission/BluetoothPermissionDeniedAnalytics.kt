package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

internal object BluetoothPermissionDeniedAnalytics {

    internal fun AnalyticsLogger.bluetoothPermissionDeniedScreen(screenName: String) = Screen(
        name = screenName,
        id = "4644246f-fb12-456e-8162-7f7d2a856b50",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.bluetoothPermissionDeniedAllowButton(buttonText: String) = Button(
        text = buttonText,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.bluetoothPermissionDeniedCancelButton(buttonText: String) = Button(
        text = buttonText,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
