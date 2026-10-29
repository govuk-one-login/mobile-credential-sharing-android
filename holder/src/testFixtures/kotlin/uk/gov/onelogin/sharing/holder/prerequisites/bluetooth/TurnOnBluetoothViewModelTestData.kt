package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object TurnOnBluetoothViewModelTestData {
    val expectedScreenEvent = Screen(
        name = "Turn on Bluetooth",
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    val expectedTurnOnBluetoothEvent = Button(
        text = "Turn on Bluetooth",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
