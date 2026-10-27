package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent

object TurnOnBluetoothViewModelTestData {
    val expectedScreenEvent = TurnOnBluetoothAnalytics.screen
        .screenViewEvent
        .asLegacyEvent()

    val expectedTurnOnBluetoothEvent = TurnOnBluetoothAnalytics.turnOnButton
        .buttonEvent
        .asLegacyEvent()
}
