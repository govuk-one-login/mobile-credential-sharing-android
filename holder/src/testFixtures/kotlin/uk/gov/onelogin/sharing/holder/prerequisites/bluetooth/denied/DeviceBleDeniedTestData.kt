package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object DeviceBleDeniedTestData {
    const val SCREEN_NAME = "Bluetooth is turned off"
    const val SCREEN_ID = "64d8556e-c6ec-4d90-812d-2ddfc834d677"
    const val TURN_ON_BUTTON_TEXT = "Turn on Bluetooth"
    const val CANCEL_BUTTON_TEXT = "Cancel and go back to documents"

    val expectedScreenEvent
        get() = Screen(
            name = SCREEN_NAME,
            id = SCREEN_ID,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedTurnOnBluetoothEvent
        get() = Button(
            text = TURN_ON_BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedCancelEvent
        get() = Button(
            text = CANCEL_BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
