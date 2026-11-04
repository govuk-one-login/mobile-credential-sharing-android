package uk.gov.onelogin.sharing.holder.awaitingresolution

import android.content.Context
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.holder.R

internal object AwaitingVerifierResolutionAnalytics {
    internal fun AnalyticsLogger.detailsSharedScreen(context: Context) = Screen(
        name = context.getEnglishString(R.string.holder_awaiting_verifier_resolution_title),
        id = "c2ccd487-c0e0-4ef8-943b-c555c4856a2c",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.goBackToDocuments(context: Context) = Button(
        text = context.getEnglishString(R.string.holder_awaiting_verifier_resolution_button),
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
