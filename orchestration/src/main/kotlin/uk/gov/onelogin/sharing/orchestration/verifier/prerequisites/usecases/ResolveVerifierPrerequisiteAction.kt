package uk.gov.onelogin.sharing.orchestration.verifier.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction.LogMessages.launchActionMessage
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@ContributesBinding(VerifierUiScope::class)
class ResolveVerifierPrerequisiteAction(
    private val orchestrator: Orchestrator.Verifier,
    private val logger: Logger
) : ResolvePrerequisiteAction<VerifierSessionState> {

    override fun resolve(launcher: ActivityResultLauncher<PrerequisiteAction>) {
        (orchestrator.verifierSessionState.value as? VerifierSessionState.Preflight)
            ?.missingPrerequisites
            ?.mapNotNull(MissingPrerequisiteV2::getAction)
            ?.let { actions ->
                if (actions.isNotEmpty() && actions.all {
                        it is PrerequisiteAction.RequestPermissions
                    }
                ) {
                    actions.mapNotNull { it as? PrerequisiteAction.RequestPermissions }
                        .reduce(PrerequisiteAction.RequestPermissions::plus)
                } else {
                    actions.firstOrNull()
                }
            }?.let { action ->
                launcher.launch(action)
                logger.debug(
                    logTag,
                    launchActionMessage(action)
                )
            }
    }
}
