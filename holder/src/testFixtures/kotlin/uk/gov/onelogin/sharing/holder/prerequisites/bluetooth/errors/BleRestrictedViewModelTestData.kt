package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object BleRestrictedViewModelTestData {
    const val SCREEN_NAME = "Bluetooth is restricted"
    const val SCREEN_ID = "19401e1a-55fb-4672-93f7-9e36eb62e22b"
    const val BUTTON_TEXT = "Go back to documents"

    val expectedScreenEvent
        get() = Screen(
            name = SCREEN_NAME,
            id = SCREEN_ID,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedGoBackEvent
        get() = Button(
            text = BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
