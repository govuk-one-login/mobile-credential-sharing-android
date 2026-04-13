package uk.gov.onelogin.sharing.orchestration.verifier.prerequisites.usecases

import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.LogMessages.updateNavigationEvent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@ContributesBinding(VerifierUiScope::class)
class RetryVerifierPrerequisites(orchestrator: Orchestrator.Verifier, private val logger: Logger) :
    RetryPrerequisitesNavigator<VerifierSessionState> {
    override val events: Flow<NavigationEvent?> =
        orchestrator.verifierSessionState
            .map { state ->
                when (state) {
                    is VerifierSessionState.ReadyToScan ->
                        NavigationEvent.PassedPrerequisites

                    is VerifierSessionState.Preflight -> {
                        if (state.missingPrerequisites.none(
                                MissingPrerequisiteV2::isRecoverable
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
