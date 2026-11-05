package uk.gov.onelogin.sharing.holder.getreadytoshare

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

internal object GetReadyToShareAnalyticsEvents {
    internal fun AnalyticsLogger.getReadyToShareScreen() = Screen(
        name = "Get ready to share",
        id = "3f5c1d0b-8068-4e19-8300-37f7bb468ff5",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.allowBluetoothButton() = Button(
        text = "Allow bluetooth",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.findOutHowSharingWorksButton() = Button(
        text = "Find out how sharing works",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
