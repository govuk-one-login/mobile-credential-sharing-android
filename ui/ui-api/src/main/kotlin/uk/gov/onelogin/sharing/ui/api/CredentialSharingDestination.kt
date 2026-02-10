package uk.gov.onelogin.sharing.ui.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface CredentialSharingDestination {
    @Serializable
    data object HolderRoute : CredentialSharingDestination

    @Serializable
    data object VerifierRoute : CredentialSharingDestination
}
