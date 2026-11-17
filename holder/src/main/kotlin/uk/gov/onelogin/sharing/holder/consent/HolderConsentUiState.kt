package uk.gov.onelogin.sharing.holder.consent

import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard

sealed interface HolderConsentUiState {
    data object Loading : HolderConsentUiState
    data class Loaded(
        val normalisedOrganizationName: String,
        val organizationName: String,
        val privacyPolicyUrl: String,
        val card: ConsentCard? = null,
        val isBusy: Boolean = false,
        val shouldShowMoreAboutDvs: Boolean = false,
    ) : HolderConsentUiState
}
