package uk.gov.onelogin.sharing.holder.gettingdetails

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object GettingDetailsNavigationExt {
    fun NavController.navigateToGettingDetailsScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(GettingDetailsRoute, options)

    internal fun NavGraphBuilder.configureGettingDetailsScreen() {
        composable<GettingDetailsRoute> {
            GettingDetailsScreen()
        }
    }
}
