package uk.gov.onelogin.sharing.holder.error

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.HolderRoutes

object UnrecoverableHolderErrorNavigationExt {
    fun NavController.navigateToUnrecoverableHolderError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableHolderErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableHolderError(navController: NavController) {
        composable<UnrecoverableHolderErrorRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }
            UnrecoverableHolderErrorScreen(
                onExitJourney = {
                    scope.launch {
                        navController.popBackStack(HolderRoutes, inclusive = true)
                    }
                }
            )
        }
    }
}
