package uk.gov.onelogin.sharing.holder.loading

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

object LoadingNavigationExt {
    fun NavController.navigateToLoadingScreen(
        type: LoadingScreenType,
        cancellationAvailable: Boolean,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(LoadingRoute(type, cancellationAvailable), options)

    internal fun NavGraphBuilder.configureLoadingScreen() {
        composable<LoadingRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LoadingRoute>()
            LoadingScreen(
                type = route.type,
                cancellationAvailable = route.cancellationAvailable
            )
        }
    }
}
