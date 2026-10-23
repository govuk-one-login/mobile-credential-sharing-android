package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

enum class WalletSharingScreen(val screenName: String, val id: String) {

    TURN_ON_BLUETOOTH(
        screenName = "Turn on Bluetooth",
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7"
    );

    val screenViewEvent: ViewEvent.Screen
        get() = ViewEvent.Screen(
            name = screenName,
            id = id,
            params = walletSharingRequiredParameters
        )
}
