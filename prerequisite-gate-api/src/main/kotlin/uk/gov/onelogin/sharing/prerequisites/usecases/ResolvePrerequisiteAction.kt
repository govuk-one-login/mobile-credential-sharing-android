package uk.gov.onelogin.sharing.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction

fun interface ResolvePrerequisiteAction<State : Any> {
    fun resolve(launcher: ActivityResultLauncher<PrerequisiteAction>)

    object LogMessages {
        fun launchActionMessage(action: PrerequisiteAction): String = "Launched action: $action"
    }
}
