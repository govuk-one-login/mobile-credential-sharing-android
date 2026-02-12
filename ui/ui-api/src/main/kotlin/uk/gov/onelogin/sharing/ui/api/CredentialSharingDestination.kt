package uk.gov.onelogin.sharing.ui.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface CredentialSharingDestination {
    @Serializable
    data object HolderRoot : CredentialSharingDestination

    @Serializable
    data object VerifierRoot : CredentialSharingDestination

    @Serializable
    data object DevMenu : CredentialSharingDestination
}
