package uk.gov.onelogin.sharing.holder.success

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object HolderSuccessNavigationExt {
    fun NavController.navigateToHolderSuccessScreen(options: NavOptionsBuilder.() -> Unit = {}) =
        navigate(HolderSuccessRoute, options)

    internal fun NavGraphBuilder.configureHolderSuccessScreen() {
        composable<HolderSuccessRoute> {
            HolderSuccessScreen()
        }
    }
}
