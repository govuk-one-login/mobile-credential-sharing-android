package uk.gov.onelogin.sharing.orchestration.prerequisites.usecases

import kotlinx.coroutines.flow.Flow

object RetryPrerequisitesNavigatorExt {
    fun <State : Any> RetryPrerequisitesNavigator.Companion.from(
        flow: Flow<RetryPrerequisitesNavigator.NavigationEvent?>
    ): RetryPrerequisitesNavigator<State> = object : RetryPrerequisitesNavigator<State> {
        override val events: Flow<RetryPrerequisitesNavigator.NavigationEvent?> = flow
    }
}
