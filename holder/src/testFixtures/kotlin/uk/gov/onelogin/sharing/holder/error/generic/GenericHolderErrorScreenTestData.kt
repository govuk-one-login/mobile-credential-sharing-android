package uk.gov.onelogin.sharing.holder.error.generic

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

object GenericHolderErrorScreenTestData {
    const val BUTTON_TEXT = "Go back to documents"
    val expectedGoBackToDocumentsButtonEvent = TrackEvent.Button(
        text = BUTTON_TEXT,
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    const val SCREEN_NAME = "Sharing is currently unavailable"

    fun expectedScreenEvent(reason: SessionError) = ViewEvent.Error(
        name = SCREEN_NAME,
        id = "9bf821af-e8ff-41cf-a311-4b6cec989315",
        endpoint = "",
        reason = reason.message,
        status = "",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
