package uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction.LogMessages.launchActionMessage

@ContributesBinding(HolderUiScope::class)
class ResolveHolderPrerequisiteAction(
    private val orchestrator: Orchestrator.Holder,
    private val logger: Logger
) : ResolvePrerequisiteAction<HolderSessionState> {

    override fun resolve(launcher: ActivityResultLauncher<PrerequisiteAction>) {
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.missingPrerequisites
            ?.mapNotNull(MissingPrerequisiteV2::getAction)
            ?.let { actions ->
                if (!actions.isEmpty() && actions.all {
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
