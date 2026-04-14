package uk.gov.onelogin.sharing.orchestration.prerequisites.usecases

import kotlinx.coroutines.flow.Flow

interface RetryPrerequisitesNavigator<State : Any> {
    val events: Flow<NavigationEvent?>

    sealed interface NavigationEvent {
        data object PassedPrerequisites : NavigationEvent
        data object UnrecoverableError : NavigationEvent
    }

    object LogMessages {
        fun updateNavigationEvent(event: NavigationEvent?): String =
            "Updated navigation event to: $event"
    }

    companion object {
        // empty to allow for extension functions
    }
}
