package uk.gov.onelogin.sharing.holder.consent

import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument

data class HolderConsentUiState(
    val privacyPolicyUrl: String? = null,
    val documents: List<ConsentDocument> = emptyList(),
    val normalisedOrganizationName: String? = null,
    val isBusy: Boolean = false
)
