package uk.gov.onelogin.sharing.holder.loading

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

object LoadingNavigationExt {
    fun NavController.navigateToLoadingScreen(
        type: LoadingScreenType,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(LoadingRoute(type), options)

    internal fun NavGraphBuilder.configureLoadingScreen() {
        composable<LoadingRoute> { backStackEntry ->
            LoadingScreen(type = backStackEntry.toRoute<LoadingRoute>().type)
        }
    }
}
