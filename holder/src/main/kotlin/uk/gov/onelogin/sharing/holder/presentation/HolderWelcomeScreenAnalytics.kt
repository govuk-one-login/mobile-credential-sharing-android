package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.onelogin.sharing.analytics.WalletSharingScreen

/**
 * Analytics identity for the Holder Welcome screen.
 *
 * The screen name and id are owned and declared by this feature module and consumed by
 * [HolderWelcomeViewModel] when sending the screen view event.
 */
internal object HolderWelcomeScreenAnalytics {
    val screen = WalletSharingScreen(
        screenName = "HolderWelcomeScreen",
        id = "29fd14a3-4390-4939-9d57-d9fe8cf61811"
    )
}
