package uk.gov.onelogin.sharing.ui.api

import kotlinx.serialization.Serializable

@Serializable
sealed interface CredentialSharingDestination {
    @Serializable
    data object CredentialSharingRoot : CredentialSharingDestination
}
