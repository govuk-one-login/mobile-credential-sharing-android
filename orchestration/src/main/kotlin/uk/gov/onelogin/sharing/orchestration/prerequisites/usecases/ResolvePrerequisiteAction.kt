package uk.gov.onelogin.sharing.orchestration.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

fun interface ResolvePrerequisiteAction<State : Any> {
    fun resolve(launcher: ActivityResultLauncher<PrerequisiteAction>)
}
