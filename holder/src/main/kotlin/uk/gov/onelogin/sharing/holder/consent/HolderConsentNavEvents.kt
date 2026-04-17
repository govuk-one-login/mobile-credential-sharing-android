package uk.gov.onelogin.sharing.holder.consent

sealed interface HolderConsentNavEvents {
    data object NavigateToGenericError : HolderConsentNavEvents
}
