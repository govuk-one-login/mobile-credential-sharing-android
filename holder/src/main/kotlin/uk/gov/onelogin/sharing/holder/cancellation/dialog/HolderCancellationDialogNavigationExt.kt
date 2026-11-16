package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.launch

data object HolderCancellationDialogNavigationExt {
    fun NavController.navigateToHolderUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationDialogRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationDialog() {
        dialog<HolderCancellationDialogRoute> {
            val scope = rememberCoroutineScope()
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current

            RemoveDestinationDialogDim()

            HolderCancellationDialogContents(
                onDismiss = {
                    scope.launch {
                        backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }

    @Composable
    private fun RemoveDestinationDialogDim() {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }
    }
}
