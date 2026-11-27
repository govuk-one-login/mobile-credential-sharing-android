package uk.gov.onelogin.sharing.holder.error

sealed interface NavigationEvent {
    data object ExitJourney : NavigationEvent
}
