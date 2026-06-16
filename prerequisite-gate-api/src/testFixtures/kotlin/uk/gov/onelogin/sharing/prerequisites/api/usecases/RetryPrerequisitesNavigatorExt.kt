package uk.gov.onelogin.sharing.prerequisites.usecases

import kotlinx.coroutines.flow.Flow
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator

object RetryPrerequisitesNavigatorExt {
    fun <State : Any> RetryPrerequisitesNavigator.Companion.from(
        flow: Flow<RetryPrerequisitesNavigator.NavigationEvent>
    ): RetryPrerequisitesNavigator<State> = object : RetryPrerequisitesNavigator<State> {
        override val events: Flow<RetryPrerequisitesNavigator.NavigationEvent> = flow
    }
}
