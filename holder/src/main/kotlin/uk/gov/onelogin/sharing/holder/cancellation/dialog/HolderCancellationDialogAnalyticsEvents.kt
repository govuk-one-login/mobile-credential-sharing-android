package uk.gov.onelogin.sharing.holder.cancellation.dialog

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * GA4 analytics identities for the "stop the check" confirmation dialog.
 *
 * The button text is supplied by the caller so that it can be resolved in English regardless of
 * the User's locale.
 */
internal object HolderCancellationDialogAnalyticsEvents {

    internal fun AnalyticsLogger.stopTheCheckButton(text: String) = Button(
        text = text,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)

    internal fun AnalyticsLogger.continueWithTheCheckButton(text: String) = Button(
        text = text,
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
