package uk.gov.onelogin.sharing.holder.error.unfulfillable

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.SuccessReason

object UnfulfillableErrorAnalyticsTestData {
    const val SCREEN_NAME = "Details requested not available"
    fun expectedScreenEvent(reason: SuccessReason = SuccessReason.UnfulfillableRequest) =
        ViewEvent.Error(
            name = SCREEN_NAME,
            id = "1c5c61b3-b8d4-4355-9be3-b26440a43b3f",
            endpoint = "",
            status = "",
            reason = reason.toString(),
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
