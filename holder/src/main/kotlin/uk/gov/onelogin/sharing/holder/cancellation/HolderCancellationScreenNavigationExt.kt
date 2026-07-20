package uk.gov.onelogin.sharing.holder.cancellation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog

data object HolderCancellationScreenNavigationExt {
    fun NavController.navigateToHolderUserCancellationScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderCancellationScreenRoute, options)

    internal fun NavGraphBuilder.configureHolderUserCancellationScreen(
        controller: NavController,
    ) {
        dialog<HolderCancellationScreenRoute> {
            HolderCancellationScreen(onCancelJourney = controller::popBackStack)
        }
    }
}
