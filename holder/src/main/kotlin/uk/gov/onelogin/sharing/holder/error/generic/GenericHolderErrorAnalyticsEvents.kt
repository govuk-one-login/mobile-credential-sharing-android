package uk.gov.onelogin.sharing.holder.error.generic

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants

internal object GenericHolderErrorAnalyticsEvents {
    internal fun AnalyticsLogger.genericHolderErrorScreen(name: String) = ViewEvent.Screen(
        name = name,
        id = "9bf821af-e8ff-41cf-a311-4b6cec989315",
        params = RequiredParameterConstants.walletSharingRequiredParameters,
    ).let(this::logEventV3Dot1)
}