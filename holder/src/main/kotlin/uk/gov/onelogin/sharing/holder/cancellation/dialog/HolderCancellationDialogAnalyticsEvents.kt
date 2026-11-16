package uk.gov.onelogin.sharing.holder.cancellation.dialog

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * GA4 analytics identities for the "stop the check" confirmation dialog.
 */
internal object HolderCancellationDialogAnalyticsEvents {

    internal fun AnalyticsLogger.stopTheCheckButton() = Button(
        text = "Stop the check",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.continueWithTheCheckButton() = Button(
        text = "Continue with the check",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
