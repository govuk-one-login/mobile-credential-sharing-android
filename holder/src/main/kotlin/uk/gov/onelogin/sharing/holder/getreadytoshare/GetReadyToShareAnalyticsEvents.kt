package uk.gov.onelogin.sharing.holder.getreadytoshare

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.screenEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

internal object GetReadyToShareAnalyticsEvents {
    internal fun AnalyticsLogger.getReadyToShareScreen() = screenEvent(
        title = "Get ready to share",
        id = "3f5c1d0b-8068-4e19-8300-37f7bb468ff5"
    )

    internal fun AnalyticsLogger.allowBluetoothButton() = buttonEvent(
        text = "Allow bluetooth"
    )

    internal fun AnalyticsLogger.findOutHowSharingWorksButton() = buttonEvent(
        text = "Find out how sharing works"
    )
}
