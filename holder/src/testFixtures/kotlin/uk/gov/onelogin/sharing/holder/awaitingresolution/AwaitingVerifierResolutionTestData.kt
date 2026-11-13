package uk.gov.onelogin.sharing.holder.awaitingresolution

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object AwaitingVerifierResolutionTestData {
    const val SCREEN_NAME = "Details shared"
    const val SCREEN_ID = "c2ccd487-c0e0-4ef8-943b-c555c4856a2c"
    const val BUTTON_TEXT = "Go back to documents"
    val expectedScreenEvent
        get() = Screen(
            name = SCREEN_NAME,
            id = SCREEN_ID,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedGoBackToDocumentsEvent
        get() = Button(
            text = BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
