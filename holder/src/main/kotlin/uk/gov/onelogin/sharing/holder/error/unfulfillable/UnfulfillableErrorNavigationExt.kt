package uk.gov.onelogin.sharing.holder.error.unfulfillable

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object UnfulfillableErrorNavigationExt {
    fun NavController.navigateToUnfulfillableError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnfulfillableErrorRoute, options)

    fun NavGraphBuilder.configureUnfulfillableErrorRoute() {
        composable<UnfulfillableErrorRoute> {

        }
    }
}