package uk.gov.onelogin.sharing.holder.cancellation.dialog

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.analytics.AnalyticsLoggerHelper.buttonEvent

/**
 * GA4 analytics identities for the "stop the check" confirmation dialog.
 *
 * The button text is supplied by the caller so that it can be resolved in English regardless of
 * the User's locale.
 */
internal object HolderCancellationDialogAnalyticsEvents {

    internal fun AnalyticsLogger.stopTheCheckButton(text: String) = buttonEvent(
        text = text
    )

    internal fun AnalyticsLogger.continueWithTheCheckButton(text: String) = buttonEvent(
        text = text
    )
}
