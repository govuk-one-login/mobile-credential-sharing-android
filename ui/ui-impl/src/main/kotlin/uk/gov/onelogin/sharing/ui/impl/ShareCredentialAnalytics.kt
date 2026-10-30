package uk.gov.onelogin.sharing.ui.impl

import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Analytics identities for the [ShareCredential] screen.
 *
 * Co-located with the screen it represents so that button identity is owned by this module
 * rather than the analytics module.
 */
internal object ShareCredentialAnalytics {
    internal fun AnalyticsLogger.closeButton() = Button(
        text = "Close",
        params = walletSharingRequiredParameters
    ).let(this::logEventV3Dot1)
}
