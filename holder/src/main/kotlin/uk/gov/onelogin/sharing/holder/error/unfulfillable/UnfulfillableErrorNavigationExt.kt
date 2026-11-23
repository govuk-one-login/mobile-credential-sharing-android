package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.holder.HolderRoutes

object UnfulfillableErrorNavigationExt {
    fun NavController.navigateToUnfulfillableError(
        options: NavOptionsBuilder.() -> Unit = {
            popUpTo<HolderRoutes> {
                inclusive = true
            }
        }
    ) = navigate(UnfulfillableErrorRoute, options)

    fun NavGraphBuilder.configureUnfulfillableErrorRoute() {

        composable<UnfulfillableErrorRoute> {
            UnfulfillableErrorScreen()
        }
    }
}