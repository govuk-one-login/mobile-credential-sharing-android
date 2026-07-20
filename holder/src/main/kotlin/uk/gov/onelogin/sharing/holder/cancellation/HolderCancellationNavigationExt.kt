package uk.gov.onelogin.sharing.holder.cancellation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog

data object HolderCancellationNavigationExt {
    fun NavController.navigateToHolderUserCancellationDialog(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationDialog(
        controller: NavController,
    ) {
        dialog<HolderCancellationRoute> {
            HolderCancellationScreen(
                onDismiss = controller::popBackStack
            )
        }
    }
}
