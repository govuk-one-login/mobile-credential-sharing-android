package uk.gov.onelogin.sharing.holder.awaitingresolution

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object AwaitingVerifierResolutionTestData {
    val expectedScreenEvent = Screen(
        name = "Details shared",
        id = "c2ccd487-c0e0-4ef8-943b-c555c4856a2c",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    val expectedGoBackToDocumentsEvent = Button(
        "Go back to documents",
        walletSharingRequiredParameters
    ).asLegacyEvent()
}
