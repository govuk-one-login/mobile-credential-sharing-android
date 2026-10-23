package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

enum class WalletSharingButton(val text: String) {

    CLOSE("Close"),
    TURN_ON_BLUETOOTH("Turn on Bluetooth");

    val buttonEvent: TrackEvent.Button
        get() = TrackEvent.Button(
            text = text,
            params = walletSharingRequiredParameters
        )
}
