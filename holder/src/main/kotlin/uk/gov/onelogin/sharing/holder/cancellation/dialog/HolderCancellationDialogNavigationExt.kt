package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.rememberCoroutineScope
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

            HolderCancellationDialogContents(
                onDismiss = {
                    scope.launch {
                        backPressedDispatcher?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            )
        }
    }
}
