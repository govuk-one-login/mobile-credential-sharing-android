package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

object AwaitingVerifierResolutionNavigationExt {
    fun NavController.navigateToAwaitingVerifierResolutionScreen(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(AwaitingVerifierResolutionRoute, options)

    internal fun NavGraphBuilder.configureAwaitingVerifierResolutionScreen() {
        composable<AwaitingVerifierResolutionRoute> {
            AwaitingVerifierResolutionScreen()
        }
    }
}
