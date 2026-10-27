package uk.gov.onelogin.sharing.ui.impl

import uk.gov.onelogin.sharing.analytics.WalletSharingButton

/**
 * Analytics identities for the [ShareCredential] screen.
 *
 * Co-located with the screen it represents so that button identity is owned by this module
 * rather than the analytics module.
 */
internal object ShareCredentialAnalytics {
    val closeButton = WalletSharingButton("Close")
}
