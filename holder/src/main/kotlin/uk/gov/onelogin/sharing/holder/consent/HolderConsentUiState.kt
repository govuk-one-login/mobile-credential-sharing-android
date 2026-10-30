package uk.gov.onelogin.sharing.holder.consent

data class HolderConsentUiState(
    val normalisedOrganizationName: String,
    val privacyPolicyUrl: String,
    val card: ConsentCard? = null,
    val isBusy: Boolean = false
)
