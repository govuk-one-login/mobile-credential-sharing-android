package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

/**
 * Describes a screen within the wallet sharing journey for analytics purposes.
 *
 * The identity ([screenName] and [id]) is owned and declared by the feature module that
 * renders the screen.
 */
data class WalletSharingScreen(val screenName: String, val id: String) {

    val screenViewEvent: ViewEvent.Screen
        get() = ViewEvent.Screen(
            name = screenName,
            id = id,
            params = walletSharingRequiredParameters
        )
}
