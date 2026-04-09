package uk.gov.onelogin.sharing.verifier.error

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import uk.gov.onelogin.sharing.verifier.VerifierRoutes

object UnrecoverableVerifierErrorNavigationExt {
    fun NavController.navigateToUnrecoverableVerifierError(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(UnrecoverableVerifierErrorRoute, options)

    internal fun NavGraphBuilder.configureUnrecoverableVerifierError(navController: NavController) {
        composable<UnrecoverableVerifierErrorRoute> {
            UnrecoverableVerifierErrorScreen(
                onExitJourney = {
                    navController.popBackStack(VerifierRoutes, inclusive = true)
                }
            )
        }
    }
}
