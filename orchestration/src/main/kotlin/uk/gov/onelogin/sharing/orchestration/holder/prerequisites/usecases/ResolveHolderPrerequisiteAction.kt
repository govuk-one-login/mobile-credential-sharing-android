package uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.ResolvePrerequisiteAction

@ContributesBinding(HolderUiScope::class)
class ResolveHolderPrerequisiteAction(private val orchestrator: Orchestrator.Holder) :
    ResolvePrerequisiteAction<HolderSessionState> {

    override fun resolve(launcher: ActivityResultLauncher<PrerequisiteAction>) {
        (orchestrator.holderSessionState.value as? HolderSessionState.Preflight)
            ?.missingPrerequisites
            ?.mapNotNull(MissingPrerequisiteV2::getAction)
            ?.forEach(launcher::launch)
    }
}
