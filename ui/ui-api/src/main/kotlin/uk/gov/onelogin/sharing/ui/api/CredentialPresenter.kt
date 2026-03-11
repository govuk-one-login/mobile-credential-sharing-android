package uk.gov.onelogin.sharing.ui.api

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

/**
 * Holder role: Presents credentials to verifiers.
 */
interface CredentialPresenter {
    val appGraph: CredentialSharingAppGraph
}
