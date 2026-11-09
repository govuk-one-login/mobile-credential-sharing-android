package uk.gov.onelogin.sharing.holder.error.generic

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object GenericHolderErrorScreenTestData {
    val expectedGoBackToDocumentsButtonEvent = TrackEvent.Button(
        text = "Go back to documents",
        params = walletSharingRequiredParameters,
    ).asLegacyEvent()

    val expectedScreenEvent = ViewEvent.Screen(
        name = "Sharing is currently unavailable",
        id = "9bf821af-e8ff-41cf-a311-4b6cec989315",
        params = walletSharingRequiredParameters,
    ).asLegacyEvent()
}