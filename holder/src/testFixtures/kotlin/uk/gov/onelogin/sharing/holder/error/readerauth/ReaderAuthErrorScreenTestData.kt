package uk.gov.onelogin.sharing.holder.error.readerauth

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.orchestration.session.SessionError

object ReaderAuthErrorScreenTestData {
    const val BUTTON_TEXT = "Go back to documents"
    val expectedGoBackToDocumentsButtonEvent
        get() = TrackEvent.Button(
            text = BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    const val SCREEN_NAME = "There is a problem"

    fun expectedScreenEvent(reason: SessionError) = ViewEvent.Error(
        name = SCREEN_NAME,
        id = "d3e44133-024c-4ce8-84bf-7bed9c01ff96",
        endpoint = "",
        reason = reason.message,
        status = "",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
