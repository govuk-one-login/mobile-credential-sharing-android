package uk.gov.onelogin.sharing.holder.success

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

object HolderSuccessNavigationExt {
    fun NavController.navigateToHolderSuccessScreen(
        immediatelyReset: Boolean = false,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(HolderSuccessRoute(immediatelyReset), options)

    internal fun NavGraphBuilder.configureHolderSuccessScreen() {
        composable<HolderSuccessRoute> { backStackEntry ->
            val route: HolderSuccessRoute = backStackEntry.toRoute()
            val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
            HolderSuccessScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                immediatelyReset = route.immediatelyReset,
                onExitJourney = {
                    backPressDispatcher
                        ?.onBackPressedDispatcher
                        ?.onBackPressed()
                }
            )
        }
    }
}
