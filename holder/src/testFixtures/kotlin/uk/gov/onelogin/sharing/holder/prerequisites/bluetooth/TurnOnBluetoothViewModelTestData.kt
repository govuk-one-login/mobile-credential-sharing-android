package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.onelogin.sharing.analytics.WalletSharingButton
import uk.gov.onelogin.sharing.analytics.WalletSharingScreen

object TurnOnBluetoothViewModelTestData {
    val expectedScreenEvent = WalletSharingScreen.TURN_ON_BLUETOOTH
        .screenViewEvent
        .asLegacyEvent()

    val expectedTurnOnBluetoothEvent = WalletSharingButton.TURN_ON_BLUETOOTH
        .buttonEvent
        .asLegacyEvent()
}
