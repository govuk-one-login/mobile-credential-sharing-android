package uk.gov.onelogin.sharing.orchestration.prerequisites.usecases

import kotlinx.coroutines.flow.Flow

interface RetryPrerequisitesNavigator<State : Any> {
    val events: Flow<NavigationEvent?>

    sealed interface NavigationEvent {
        data object PassedPrerequisites : NavigationEvent
        data object UnrecoverableError : NavigationEvent
    }

    companion object {
        // empty to allow for extension functions
    }
}
