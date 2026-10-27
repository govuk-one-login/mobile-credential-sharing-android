package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Describes a button within the wallet sharing journey for analytics purposes.
 *
 * The identity ([text]) is owned and declared by the feature module that renders the
 * button.
 */
data class WalletSharingButton(val text: String) {

    val buttonEvent: TrackEvent.Button
        get() = TrackEvent.Button(
            text = text,
            params = walletSharingRequiredParameters
        )
}
