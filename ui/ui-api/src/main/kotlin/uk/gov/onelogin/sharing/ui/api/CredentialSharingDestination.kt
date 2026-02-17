package uk.gov.onelogin.sharing.ui.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface CredentialSharingDestination {
    @Serializable
    data object Holder : CredentialSharingDestination

    @Serializable
    data object Verifier : CredentialSharingDestination
}
