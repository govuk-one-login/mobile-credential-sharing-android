package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog

data object HolderCancellationDialogNavigationExt {
    fun NavController.navigateToHolderUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationDialogRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationDialog(
        controller: NavController,
    ) {
        dialog<HolderCancellationDialogRoute> {
            HolderCancellationScreen(
                onDismiss = controller::popBackStack
            )
        }
    }
}
