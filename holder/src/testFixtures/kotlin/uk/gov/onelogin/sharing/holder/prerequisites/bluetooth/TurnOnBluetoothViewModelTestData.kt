package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object TurnOnBluetoothViewModelTestData {
    const val SCREEN_NAME = "Turn on Bluetooth"
    const val SCREEN_ID = "d64962b9-def5-4b13-9c3c-128d51353fe7"
    const val BUTTON_TEXT = "Turn on Bluetooth"
    val expectedScreenEvent
        get() = Screen(
            name = SCREEN_NAME,
            id = SCREEN_ID,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedTurnOnBluetoothEvent
        get() = Button(
            text = BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
