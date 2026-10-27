package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.onelogin.sharing.analytics.WalletSharingButton
import uk.gov.onelogin.sharing.analytics.WalletSharingScreen

/**
 * Analytics identities for the Turn on Bluetooth screen.
 *
 * Co-located with the screen it represents so that screen/button identity (names and IDs)
 * is owned by this feature module
 */
internal object TurnOnBluetoothAnalytics {
    val screen = WalletSharingScreen(
        screenName = "Turn on Bluetooth",
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7"
    )

    val turnOnButton = WalletSharingButton("Turn on Bluetooth")
}
