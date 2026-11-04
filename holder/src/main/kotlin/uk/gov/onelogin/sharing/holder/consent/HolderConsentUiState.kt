package uk.gov.onelogin.sharing.holder.consent

data class HolderConsentUiState(
    val privacyPolicyUrl: String? = null,
    val card: ConsentCard? = null,
    val normalisedOrganizationName: String? = null,
    val isBusy: Boolean = false
)
