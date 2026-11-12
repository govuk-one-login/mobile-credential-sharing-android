package uk.gov.onelogin.sharing.holder.consent

sealed interface HolderConsentUiState {
    data object Loading : HolderConsentUiState
    data class ConfirmConsent(
        val normalisedOrganizationName: String,
        val organizationName: String,
        val privacyPolicyUrl: String,
        val card: ConsentCard? = null,
        val isBusy: Boolean = false,
    ) : HolderConsentUiState

    data class MoreAboutDvs(
        val normalisedOrganizationName: String,
        val privacyPolicyUrl: String
    ) : HolderConsentUiState
}
