package uk.gov.onelogin.sharing.holder.awaitingresolution

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

internal object AwaitingVerifierResolutionAnalytics {
    internal fun AnalyticsLogger.detailsSharedScreen() = Screen(
        name = "Details shared",
        id = "c2ccd487-c0e0-4ef8-943b-c555c4856a2c",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.goBackToDocuments() = Button(
        "Go back to documents",
        walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
