package uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases

import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.LogMessages.updateNavigationEvent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent

@ContributesBinding(HolderUiScope::class)
class RetryHolderPrerequisites(orchestrator: Orchestrator.Holder, private val logger: Logger) :
    RetryPrerequisitesNavigator<HolderSessionState> {
    override val events: Flow<NavigationEvent?> =
        orchestrator.holderSessionState
            .map { state ->
                when (state) {
                    is HolderSessionState.PresentingEngagement ->
                        NavigationEvent.PassedPrerequisites

                    is HolderSessionState.Preflight -> {
                        if (state.missingPrerequisites.none(
                                MissingPrerequisite::isRecoverable
                            )
                        ) {
                            NavigationEvent.UnrecoverableError
                        } else {
                            null
                        }
                    }

                    else -> null
                }.also {
                    logger.debug(
                        logTag,
                        updateNavigationEvent(it)
                    )
                }
            }
}
