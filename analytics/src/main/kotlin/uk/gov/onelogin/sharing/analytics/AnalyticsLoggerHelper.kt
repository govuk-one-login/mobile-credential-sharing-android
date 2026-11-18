package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Error as ViewError
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object AnalyticsLoggerHelper {
    fun AnalyticsLogger.buttonEvent(
        text: String,
        params: RequiredParameters = walletSharingRequiredParameters
    ) = Button(
        text = text,
        params = params
    ).let(this::logEventV3Dot1)

    @Suppress("LongParameterList")
    fun AnalyticsLogger.errorScreenEvent(
        name: String,
        id: String,
        reason: String,
        endpoint: String = "",
        status: String = "",
        params: RequiredParameters = walletSharingRequiredParameters
    ) = ViewError(
        name = name,
        id = id,
        endpoint = endpoint,
        reason = reason,
        status = status,
        params = params
    ).let(this::logEventV3Dot1)

    fun AnalyticsLogger.screenEvent(
        title: String,
        id: String,
        params: RequiredParameters = walletSharingRequiredParameters
    ) = Screen(
        name = title,
        id = id,
        params = params
    ).let(this::logEventV3Dot1)
}
